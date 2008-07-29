package org.griphyn.vdl.engine;

import java.util.ArrayList;
import java.util.HashMap;

import org.globus.swift.language.FormalParameter;


public class ProcedureSignature {
	
	private String name;
	private ArrayList inputArgs;
	private ArrayList outputArgs;
	private boolean anyNumOfInputArgs;
	private boolean anyNumOfOutputArgs; /* this is maybe unnecessary*/
	
	
	public ProcedureSignature(String name) {
		this.name = name;
		inputArgs = new ArrayList();
		outputArgs = new ArrayList();
		anyNumOfInputArgs = false;
		anyNumOfOutputArgs = false;
	}
	
	public String getName() {
		return name;
	}
	
	public void addInputArg(FormalArgumentSignature inputArg) {
		inputArgs.add(inputArg);
	}
	
	public void addOutputArg(FormalArgumentSignature outputArg) {
		outputArgs.add(outputArg);
	}
	
	public void setAnyNumOfInputArgs() {
		anyNumOfInputArgs = true;
	}
	
	public void setAnyNumOfOutputArgs() {
		anyNumOfOutputArgs = true;
	}
	
	public boolean getAnyNumOdInputArgs() {
		return anyNumOfInputArgs;
	}
	
	public boolean getAnyNumOdOutputArgs() {
		return anyNumOfOutputArgs;
	}
	
	public int sizeOfInputArray() {
		return inputArgs.size();
	}
	
	public int sizeOfOutputArray() {
		return outputArgs.size();
	}
	
	public FormalArgumentSignature[] getInputArray() {
		return (FormalArgumentSignature[]) inputArgs.toArray();
	}
	
	public FormalArgumentSignature[] getOutputArray() {
		return (FormalArgumentSignature[]) outputArgs.toArray();
	}
	
	public FormalArgumentSignature getInputArray(int i) {
		return (FormalArgumentSignature) inputArgs.get(i);
	}
	
	public FormalArgumentSignature getOutputArray(int i) {
		return (FormalArgumentSignature) outputArgs.get(i);
	}
	
	public void setInputArgs(FormalParameter[] fp) {
		for (int i = 0; i < fp.length; i++) {
			FormalArgumentSignature fas = new FormalArgumentSignature(fp[i].getType().getLocalPart(),
					                                                  fp[i].getName());
			fas.setOptional(!fp[i].isNil());
			this.addInputArg(fas);
		}
	}
	
	public void setOutputArgs(FormalParameter[] fp) {
		for (int i = 0; i < fp.length; i++) {
			FormalArgumentSignature fas = new FormalArgumentSignature(fp[i].getType().getLocalPart(),
					                                                  fp[i].getName());
			/* fas.setOptional(!fp[i].isNil()); */
			/* unnecessary because output arg can not be optional */
			this.addOutputArg(fas);
		}
	}
	
	public static HashMap makeProcedureSignatures() {
		HashMap proceduresMap = new HashMap();
		
		ProcedureSignature readData = new ProcedureSignature("readData");
		FormalArgumentSignature rdInputArg = new FormalArgumentSignature(true);
		readData.addInputArg(rdInputArg);
		FormalArgumentSignature rdOutputArg = new FormalArgumentSignature(true);
		readData.addOutputArg(rdOutputArg);		
		proceduresMap.put("readData", readData);
		
		ProcedureSignature print = new ProcedureSignature("print");
		print.setAnyNumOfInputArgs();
		proceduresMap.put("print", print);
		
		ProcedureSignature trace = new ProcedureSignature("trace");
		trace.setAnyNumOfInputArgs();
		proceduresMap.put("trace", trace);
		
		return proceduresMap;
	}
	
	public static HashMap makeFunctionSignatures () {
		HashMap functionsMap = new HashMap();
		
		ProcedureSignature arg = new ProcedureSignature("arg");
		FormalArgumentSignature argIn1 = new FormalArgumentSignature("string");
		arg.addInputArg(argIn1);
		FormalArgumentSignature argIn2 = new FormalArgumentSignature("string");		
		argIn2.setOptional(true);
		arg.addInputArg(argIn2);
		FormalArgumentSignature argOut1 = new FormalArgumentSignature("string");
		arg.addOutputArg(argOut1);
		functionsMap.put(arg.getName(), arg);
		
		ProcedureSignature extractint = new ProcedureSignature("extractint");
		FormalArgumentSignature extractintIn1 = new FormalArgumentSignature(true); /* file can be specified as any type */
		extractint.addInputArg(extractintIn1);
		FormalArgumentSignature extractintOut1 = new FormalArgumentSignature("int");
		extractint.addOutputArg(extractintOut1);
		functionsMap.put(extractint.getName(), extractint);
		
		ProcedureSignature filename = new ProcedureSignature("filename");
		FormalArgumentSignature filenameIn1 = new FormalArgumentSignature(true); /* file can be specified as any type */
		filename.addInputArg(filenameIn1);
		FormalArgumentSignature filenameOut1 = new FormalArgumentSignature("string");
		filename.addOutputArg(filenameOut1);
		functionsMap.put(filename.getName(), filename);
		
		ProcedureSignature filenames = new ProcedureSignature("filenames");
		FormalArgumentSignature filenamesIn1 = new FormalArgumentSignature(true); /* file can be specified as any type */
		filenames.addInputArg(filenamesIn1);
		FormalArgumentSignature filenamesOut1 = new FormalArgumentSignature("string[]"); /* i think this is what it returns */
		filenames.addOutputArg(filenamesOut1);
		functionsMap.put(filenames.getName(), filenames);
		
		ProcedureSignature regexp = new ProcedureSignature("regexp");
		FormalArgumentSignature regexpIn1 = new FormalArgumentSignature("string");
		regexp.addInputArg(regexpIn1);
		FormalArgumentSignature regexpIn2 = new FormalArgumentSignature("string");
		regexp.addInputArg(regexpIn2);
		FormalArgumentSignature regexpIn3 = new FormalArgumentSignature("string");
		regexp.addInputArg(regexpIn3);
		FormalArgumentSignature regexpOut1 = new FormalArgumentSignature("string");
		regexp.addOutputArg(regexpOut1);
		functionsMap.put(regexp.getName(), regexp);
		
		ProcedureSignature strcat = new ProcedureSignature("strcat");
		strcat.setAnyNumOfInputArgs();
		FormalArgumentSignature strcatOut1 = new FormalArgumentSignature("string");
		strcat.addOutputArg(strcatOut1);
		functionsMap.put(strcat.getName(), strcat);
		
		ProcedureSignature strcut = new ProcedureSignature("strcut");
		FormalArgumentSignature strcutIn1 = new FormalArgumentSignature("string");
		strcut.addInputArg(strcutIn1);
		FormalArgumentSignature strcutIn2 = new FormalArgumentSignature("string");
		strcut.addInputArg(strcutIn2);
		FormalArgumentSignature strcutOut1 = new FormalArgumentSignature("string");
		strcut.addOutputArg(strcutOut1);
		functionsMap.put(strcut.getName(), strcut);
		
		ProcedureSignature toint = new ProcedureSignature("toint");
		FormalArgumentSignature tointIn1 = new FormalArgumentSignature("string");
		toint.addInputArg(tointIn1);
		FormalArgumentSignature toOut1 = new FormalArgumentSignature("int");
		toint.addOutputArg(toOut1);
		functionsMap.put(toint.getName(), toint);
		
		return functionsMap;
	}
	
}
