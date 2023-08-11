package org.mule.extension.parquet;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import org.mule.runtime.api.event.Event;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.mule.functional.junit4.MuleArtifactFunctionalTestCase;
import org.junit.Test;

public class ParquetOperationsTestCase extends MuleArtifactFunctionalTestCase {

  /**
   * Specifies the mule config xml with the flows that are going to be executed in the tests, this file lives in the test resources.
   */
  @Override
  protected String getConfigFile() {
    return "test-mule-config.xml";
  }

  @Test
  public void executeGetParquetSchemaOperation() throws Exception {
    Event response;
    try (FileInputStream fis = new FileInputStream(new File("src/test/resources/test.parquet"))) {
      response = (flowRunner("getParquetSchemaFlow").withVariable("file",fis).run());
      assertThat(response.getMessage().getPayload().getDataType().getMediaType().getPrimaryType(), is("application"));
    } catch (IOException e) {
        e.printStackTrace();
    }
  }
}
