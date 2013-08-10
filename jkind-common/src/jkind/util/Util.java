package jkind.util;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jkind.JKindException;
import jkind.lustre.NamedType;
import jkind.lustre.Node;
import jkind.lustre.RecordType;
import jkind.lustre.SubrangeIntType;
import jkind.lustre.Type;
import jkind.lustre.VarDecl;
import jkind.lustre.values.BooleanValue;
import jkind.lustre.values.IntegerValue;
import jkind.lustre.values.RealValue;
import jkind.lustre.values.Value;

public class Util {
	public static List<VarDecl> getVarDecls(Node node) {
		List<VarDecl> decls = new ArrayList<>();
		decls.addAll(node.inputs);
		decls.addAll(node.outputs);
		decls.addAll(node.locals);
		return decls;
	}

	public static Map<String, Type> getTypeMap(Node node) {
		Map<String, Type> map = new HashMap<>();
		for (VarDecl v : getVarDecls(node)) {
			map.put(v.id, v.type);
		}
		return map;
	}

	public static List<String> getIds(List<VarDecl> decls) {
		List<String> ids = new ArrayList<>();
		for (VarDecl decl : decls) {
			ids.add(decl.id);
		}
		return ids;
	}

	public static Map<String, Node> getNodeTable(List<Node> nodes) {
		Map<String, Node> nodeTable = new HashMap<>();
		for (Node node : nodes) {
			nodeTable.put(node.id, node);
		}
		return nodeTable;
	}
	
	public static String getName(Type type) {
		if (type instanceof NamedType) {
			NamedType namedType = (NamedType) type;
			return namedType.name;
		} else if (type instanceof SubrangeIntType) {
			return "int";
		} else {
			throw new IllegalArgumentException("Cannot find name for type " + type);
		}
	}

	public static Value parseValue(String type, String value) {
		if (type.equals("bool")) {
			if (value.equals("0") || value.equals("false")) {
				return BooleanValue.FALSE;
			} else if (value.equals("1") || value.equals("true")) {
				return BooleanValue.TRUE;
			}
		} else if (type.equals("int")) {
			return new IntegerValue(new BigInteger(value));
		} else if (type.equals("real")) {
			String[] strs = value.split("/");
			if (strs.length <= 2) {
				BigInteger num = new BigInteger(strs[0]);
				BigInteger denom = strs.length > 1 ? new BigInteger(strs[1]) : BigInteger.ONE;
				return new RealValue(num, denom);
			}
		}

		throw new JKindException("Unable to parse " + value + " as " + type);
	}
	
	public static Type resolveType(Type type, Map<String, Type> map) {
		if (type instanceof NamedType) {
			NamedType namedType = (NamedType) type;
			if (namedType.isBuiltin()) {
				return namedType;
			} else {
				return map.get(namedType.name);
			}
		} else if (type instanceof SubrangeIntType) {
			return type;
		} else if (type instanceof RecordType) {
			RecordType recordType = (RecordType) type;
			Map<String, Type> resolvedFields = new HashMap<>();
			for (String field : recordType.fields.keySet()) {
				resolvedFields.put(field, resolveType(recordType.fields.get(field), map));
			}
			return new RecordType(recordType.location, resolvedFields);
		} else {
			return null;
		}
	}
}