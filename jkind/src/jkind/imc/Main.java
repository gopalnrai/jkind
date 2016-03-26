package jkind.imc;

import java.io.IOException;

import jkind.SolverOption;
import jkind.analysis.StaticAnalyzer;
import jkind.lustre.Node;
import jkind.lustre.Program;
import jkind.lustre.builders.NodeBuilder;
import jkind.slicing.DependencyMap;
import jkind.slicing.LustreSlicer;
import jkind.translation.Translate;

import org.antlr.v4.runtime.RecognitionException;

public class Main {
	public static void main(String[] args) throws RecognitionException, IOException {
		Program program = jkind.Main.parseLustre(args[0]);
		StaticAnalyzer.check(program, SolverOption.YICES);

		Node main = Translate.translate(program);
		DependencyMap depMap = new DependencyMap(main, main.properties);

		for (String property : main.properties) {
			Node single = new NodeBuilder(main).clearProperties().addProperty(property).build();
			single = LustreSlicer.slice(single, depMap);
			long start = System.currentTimeMillis();
			System.out.println("Property: " + property);
			summary(new Imc(single).imcMain());
			long stop = System.currentTimeMillis();
			System.out.println((stop - start) / 1000.0);
			System.out.println();
		}
	}

	private static void summary(int k) {
		System.out.println();
		if (k < 0) {
			System.out.println("VALID");
		} else {
			System.out.println("INVALID, COUNTEREXAMPLE OF LENGTH: " + k);
		}
	}
}