package com.networknt.jsonoverlay.model;

import java.io.IOException;
import java.net.URL;

import com.networknt.jsonoverlay.Overlay;
import com.networknt.jsonoverlay.ReferenceManager;
import com.networknt.jsonoverlay.model.impl.TestModelImpl;
import com.networknt.jsonoverlay.model.intf.TestModel;

public class TestModelParser {

	public static TestModel parse(URL url) throws IOException {
		ReferenceManager manager = new ReferenceManager(url);
		TestModel model = (TestModel) TestModelImpl.factory.create(manager.loadDoc(), null, manager);
		((TestModelImpl) model)._setCreatingRef(manager.getReference(url.toString()));
		return Overlay.of(model).get();
	}
}
