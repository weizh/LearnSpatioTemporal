package com.wynnzh.learner;

import edu.cmu.lti.weizh.docmodel.DataSet;
import edu.cmu.lti.weizh.train.AbstractPercEval;

public class ActiveEvaluator extends AbstractPercEval<String, DataSet, ActiveLearner> {

	protected ActiveEvaluator(ActiveLearner trainer) {
		super(trainer);
	}

}
