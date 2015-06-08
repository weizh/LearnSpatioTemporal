package com.wynnzh.learner;

import com.wynnzh.data.WCDataSet;

import edu.cmu.lti.weizh.train.AbstractPercEval;

public class ActiveEvaluator extends AbstractPercEval<String, WCDataSet, ActiveLearner> {

	protected ActiveEvaluator(ActiveLearner trainer) {
		super(trainer);
	}

}
