package com.wynnzh.learner;

import edu.cmu.lti.weizh.docmodel.DataSet;
import edu.cmu.lti.weizh.train.AbstractPercEval;

public class ActiveEvaluator extends AbstractPercEval<String, DataSet, ActiveNERLearner> {

	protected ActiveEvaluator(ActiveNERLearner trainer) {
		super(trainer);
	}

}
