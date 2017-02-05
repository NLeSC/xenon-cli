package nl.esciencecenter.xenon.cli;

import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertEquals;

public class MainTest {
    @Test
    public void buildXenonProperties() throws Exception {
        Main main = new Main();
        Map<String, Object> attrs = new HashMap<>();
        List<String> propsIn = Arrays.asList(new String[]{"KEY1=VAL1", "KEY2=VAL2"});
        attrs.put("props", propsIn);
        Namespace ns = new Namespace(attrs);

        Map<String, String> result = main.buildXenonProperties(ns);
        Map<String, String> expected = new HashMap<>();
        expected.put("KEY1", "VAL1");
        expected.put("KEY2", "VAL2");
        assertEquals(expected, result);
    }

    @Test(expected = ArgumentParserException.class)
    public void mainRootHelp() throws Exception {
        Main main = new Main();
        ArgumentParser parser = main.buildArgumentParser();

        String[] args = {};
        parser.parseArgs(args);
    }
}