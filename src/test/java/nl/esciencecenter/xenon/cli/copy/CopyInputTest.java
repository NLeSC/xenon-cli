package nl.esciencecenter.xenon.cli.copy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import nl.esciencecenter.xenon.XenonException;
import org.junit.Test;

public class CopyInputTest {

    @Test
    public void isStream_localdash_true() throws Exception {
        CopyInput copyInput = new CopyInput("file", null, "-", null);
        assertTrue(copyInput.isStream());
    }

    @Test
    public void isStream_localpath_false() throws Exception {
        CopyInput copyInput = new CopyInput("file", null, "/tmp/bla", null);
        assertFalse(copyInput.isStream());
    }

    @Test
    public void toJson() throws XenonException {
        CopyInput copyInput = new CopyInput("file", null, "/tmp/bla", null);
        Gson gson = new GsonBuilder().create();
        String result = gson.toJson(copyInput);
        String expected = "{\"adaptor\":\"file\",\"path\":\"/tmp/bla\",\"stream\":false}";
        assertEquals(expected, result);
    }
}