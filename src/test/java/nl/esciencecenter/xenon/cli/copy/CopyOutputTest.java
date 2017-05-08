package nl.esciencecenter.xenon.cli.copy;

import static org.junit.Assert.assertEquals;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.Before;
import org.junit.Test;

public class CopyOutputTest {
    private CopyOutput copyOutput;

    @Before
    public void setUp() throws Exception {
        CopyInput source = new CopyInput("local", "/", "/source", null);
        CopyInput target = new CopyInput("sftp", "localhost", "/target", null);
        copyOutput = new CopyOutput(source, target);
    }

    @Test
    public void test_toString() throws Exception {
        String output = copyOutput.toString();

        String expected = "Copied '/source' from location '/' to  '/target' to location 'localhost'";
        assertEquals(expected, output);
    }

    @Test
    public void toJson() {
        Gson gson = new GsonBuilder().create();
        String result = gson.toJson(copyOutput);
        String expected = "{\"target\":{\"scheme\":\"sftp\",\"location\":\"localhost\",\"path\":\"/target\",\"stream\":false},\"source\":{\"scheme\":\"local\",\"location\":\"/\",\"path\":\"/source\",\"stream\":false}}";
        assertEquals(expected, result);
    }

    @Test
    public void test_toString_sourcelocationnull() throws Exception {
        CopyInput source = new CopyInput("local", null, "/source", null);
        CopyInput target = new CopyInput("sftp", "localhost", "/target", null);
        copyOutput = new CopyOutput(source, target);
        String output = copyOutput.toString();

        String expected = "Copied '/source' from location 'local' to  '/target' to location 'localhost'";
        assertEquals(expected, output);
    }

    @Test
    public void test_toString_targetlocationnull() throws Exception {
        CopyInput source = new CopyInput("sftp", "localhost", "/source", null);
        CopyInput target = new CopyInput("local", null, "/target", null);
        copyOutput = new CopyOutput(source, target);
        String output = copyOutput.toString();

        String expected = "Copied '/source' from location 'localhost' to  '/target' to location 'local'";
        assertEquals(expected, output);
    }

}