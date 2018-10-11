+ Class {
	//object variables that have no corresponding
	//methods
	privateInstVars{arg includeSuperclass = false;
		var result, myMethods;
		if(includeSuperclass, {
			result = instVarNames.as(Set)
				++ this.superclass.privateInstVars(true);

			myMethods = this.methods ++ this.superclasses.reject;
		}, {
			result = instVarNames.as(Set)
				- this.superclass.instVarNames.as(Set);

			myMethods = this.methods;
		});

		result = result - myMethods.collect(_.name).as(Set);
		result = result - myMethods.collect(_.name).select(_.isSetter)
			.collect(_.asGetter);

		^result;
	}

	setterInstVars{arg includeSuperclass = false;
		var result;
		if(includeSuperclass, {
			result = instVarNames.as(Set);
		}, {
			result = instVarNames.as(Set)
				- this.superclass.instVarNames.as(Set);
		});

		result = result.sect(
			this.methods.collect(_.name).select(_.isSetter)
				.collect(_.asGetter).as(Set)
			);
		^result;
	}

	getterInstVars{arg includeSuperclass = false;
		var result;
		if(includeSuperclass, {
			result = instVarNames.as(Set);
		}, {
			result = instVarNames.as(Set)
				- this.superclass.instVarNames.as(Set);
		});

		result = result.sect(this.methods.collect(_.name)
			.select({arg it; it.isSetter.not;})
			.as(Set));

		^result;
	}

	instVars{
		^this.instVarNames.as(Set)
			- this.superclass.instVarNames.as(Set);
	}

	definedMethods{arg includeSuperclass = false;
		^this.methods.select(_.isDefined);
	}

	synthesizedMethods{arg includeSuperclass = false;
		^this.methods.select(_.isSynthesized);
	}


	//Find all the superclass method instances that this
	//class overrides.
	overridingMethods{
		var result;
		result = this.methods.select({arg method;
			method.isOverridingSuperclass;
		});
		^result;
	}

	dotUMLString{
		var str;
		str = "\t% [".format(this.name) << "\n";
		str = str ++ "\t\tlabel = ";
		str = str ++ "\"{%|%|%}\"".format(
			this.name,
			this.instVarDotUMLString,
			this.methodDotUMLString
		);
		str = str ++ "\n";
		str = str ++ "\t]" << "\n";
		^str;
	}

	instVarDotUMLString{
		var result;
		this.classVarNames.do({arg instVarName;
			result = result ++ "% %\\l".format(
				"*", //getset
				instVarName
			);
		});

		this.instVars.do({arg instVarName;
			result = result ++ "% %\\l".format(
				"-", //getset
				instVarName
			);
		});
		^result;
	}

	methodDotUMLString{
		var result;
		var escapeChars = {arg str;
			var result = str;
			[$\\, $", $>, $<, $|].do({arg it;
				result = result.escapeChar(it);
			});
			result;
		};
		this.methods.do({arg it;
			var args = "";
			if(it.argNames.size > 1, {
				args = it.argumentString;
			});
			result = result ++ "* %( % )\\l".format(
				escapeChars.value(it.name.asString),
				escapeChars.value(args)
			);
			escapeChars.value(result);
		});

		this.methods.reject({arg it;
			it.hasCorrespondingInstVar;
		}).do({arg it;
			var args = "";
			if(it.argNames.size > 1, {
				args = it.argumentString;
			});
			result = result ++ "%( % )%%\\l".format(
				escapeChars.value(it.name.asString),
				escapeChars.value(args),
				if(it.isOverridingSuperclass, {
					" @overrides %".format(
						it.findOverriddenMethod.ownerClass.name)
				}, {
					"";
				}),
				if(it.isExtensionMethod, {
					" @extension"
				}, {
					"";
				})
			);
		});
		^result;
	}
}
