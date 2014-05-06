package jkind.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import jkind.JKindException;
import jkind.interval.Interval;
import jkind.lustre.EnumType;
import jkind.lustre.Function;
import jkind.lustre.NamedType;
import jkind.lustre.Node;
import jkind.lustre.SubrangeIntType;
import jkind.lustre.Type;
import jkind.lustre.VarDecl;
import jkind.lustre.values.BooleanValue;
import jkind.lustre.values.EnumValue;
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
	
	public static List<VarDecl> getVarDecls(Function function) {
		List<VarDecl> decls = new ArrayList<>();
		decls.addAll(function.inputs);
		decls.addAll(function.outputs);
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
	
	public static List<Type> getTypes(List<VarDecl> decls) {
		List<Type> types = new ArrayList<>();
		for (VarDecl decl : decls) {
			types.add(decl.type);
		}
		return types;
	}

	public static Map<String, Node> getNodeTable(List<Node> nodes) {
		Map<String, Node> nodeTable = new HashMap<>();
		for (Node node : nodes) {
			nodeTable.put(node.id, node);
		}
		return nodeTable;
	}

	public static Map<String, Function> getFunctionTable(List<Function> functions) {
		Map<String, Function> functionTable = new HashMap<>();
		for (Function function : functions) {
			functionTable.put(function.id, function);
		}
		return functionTable;
	}

	/*
	 * Get the name of the type as modeled by the SMT solvers
	 */
	public static String getName(Type type) {
		if (type instanceof NamedType) {
			NamedType namedType = (NamedType) type;
			return namedType.name;
		} else if (type instanceof SubrangeIntType || type instanceof EnumType) {
			return "int";
		} else {
			throw new IllegalArgumentException("Cannot find name for type " + type);
		}
	}

	public static Value parseValue(String type, String value) {
		switch (type) {
		case "bool":
			if (value.equals("0") || value.equals("false")) {
				return BooleanValue.FALSE;
			} else if (value.equals("1") || value.equals("true")) {
				return BooleanValue.TRUE;
			}
			break;

		case "int":
			return new IntegerValue(new BigInteger(value));

		case "real":
			String[] strs = value.split("/");
			if (strs.length <= 2) {
				BigInteger num = new BigInteger(strs[0]);
				BigInteger denom = strs.length > 1 ? new BigInteger(strs[1]) : BigInteger.ONE;
				return new RealValue(new BigFraction(num, denom));
			}
			break;

		default:
			return new EnumValue(value);
		}

		throw new JKindException("Unable to parse " + value + " as " + type);
	}

	public static Type resolveType(Type type, Map<String, Type> map) {
		return TypeResolver.resolve(type, map);
	}

	public static void writeToFile(String content, File file) throws IOException {
		try (Writer writer = new BufferedWriter(new FileWriter(file))) {
			writer.append(content);
		}
	}

	public static boolean isWindows() {
		return System.getProperty("os.name").startsWith("Windows");
	}

	public static boolean isArbitrary(Value value) {
		if (value == null) {
			return true;
		} else if (value instanceof Interval) {
			return ((Interval) value).isArbitrary();
		}
		return false;
	}

	/**
	 * In SMT solvers, integer division behaves differently than in Java. In
	 * particular, for -5 div 3 java says '-1' and SMT solvers say '-2'
	 */
	public static BigInteger smtDivide(BigInteger a, BigInteger b) {
		return a.subtract(a.mod(b)).divide(b);
	}
	
	public static String getBase(String name) {
		return name.substring(0, name.indexOf('.'));
	}

	public static <T> List<T> safeList(List<? extends T> original) {
		if (original == null) {
			return Collections.emptyList();
		} else {
			return Collections.unmodifiableList(new ArrayList<>(original));
		}
	}

	public static <T> SortedMap<String, T> safeStringSortedMap(Map<String, T> original) {
		TreeMap<String, T> map = new TreeMap<>(new StringNaturalOrdering());
		if (original != null) {
			map.putAll(original);
		}
		return Collections.unmodifiableSortedMap(map);
	}
}
