package org.brain2.test.io;

import static org.junit.Assert.fail;

import java.io.IOException;

import org.brain2.test.vneappcrawler.ImporterConfigs;
import org.brain2.ws.core.utils.FileUtils;
import org.junit.Assert;
import org.junit.Test;

import com.google.gson.Gson;

public class TestLoadConfigs {

	@Test
	public void loadImporterDbConfigs() {
		String json = null;
		try {
			json = FileUtils.readFileAsString("/importer-configs.json");
			System.out.println(json);
			Gson gson = new Gson();
			ImporterConfigs configs =  gson.fromJson(json, ImporterConfigs.class);			
			Assert.assertEquals("vnemobile", configs.getUsername());
			Assert.assertEquals("vnemobile@123", configs.getPassword());
			System.out.println(configs.getMySQLConnectionUrl());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail(e.getMessage());
		}
		Assert.assertNotNull(json);
	}

}
