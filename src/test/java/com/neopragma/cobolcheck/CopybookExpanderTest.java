package com.neopragma.cobolcheck;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;


@ExtendWith(MockitoExtension.class)
public class CopybookExpanderTest implements Constants, StringHelper {
    private static String pathToTestCobolSources;
    private static String pathToTestCobolCopybooks;

    private CopybookExpander copybookExpander;
    private String expectedResult;
    private String testCopybookFilename;
    private String testCopybookBasename;
    private static String copybookFilenameSuffix;

    @Mock
    private static Messages messages;
    @InjectMocks
    private static Config config;

    @BeforeAll
    public static void oneTimeSetup() {
//TODO: Solve the mystery - mock messages object is always null
//  We don't need a real Messages object for this test class
//        when(messages.get(anyString())).thenReturn(EMPTY_STRING);
//        doReturn(EMPTY_STRING).when(messages).get(anyString(), anyString());
//        config = new Config(messages);
        config = new Config(new Messages());

        config.load("testconfig.properties");
        pathToTestCobolSources = getPathFor("application.source.directory", "testcobolsources");
        pathToTestCobolCopybooks = getPathFor("application.copybook.directory", "testcobolcopybooks");
        copybookFilenameSuffix = config.getApplicationFilenameSuffix();
    }

    @BeforeEach
    public void commonSetup() {
        copybookExpander = new CopybookExpander(config, messages);
        testCopybookFilename = EMPTY_STRING;
        expectedResult = EMPTY_STRING;

    }

    @Test
    public void it_expands_a_simple_copybook() throws IOException {
        Writer expandedSource =
                runTestCase("COPY001-padded", "COPY001-padded");
        assertEquals(expectedResult, expandedSource.toString());
    }

    @Test
    public void it_expands_nested_copybooks_one_level_deep() throws IOException {
        Writer expandedSource =
                runTestCase("COPY002-padded", "EX002-padded");
        assertEquals(expectedResult, runTestCase("COPY002-padded", "EX002-padded").toString());
    }

    @Test
    public void it_expands_nested_copybooks_three_levels_deep() throws IOException {
        Writer expandedSource =
                runTestCase("COPY005-padded", "EX005-padded");
        assertEquals(expectedResult, expandedSource.toString());
    }

    @Test
    public void it_handles_lower_case_and_mixed_case_code() throws IOException {
        Writer expandedSource =
                runTestCase("mixed005-padded", "mixedex005-padded");
        assertEquals(expectedResult, expandedSource.toString());
    }

    @Test
    public void it_handles_copy_replacing_with_whole_words() throws IOException {
        Writer expandedSource =
                runTestCase("COPYR001-padded", "EXR001-padded");
        assertEquals(expectedResult, expandedSource.toString());
    }


    private Writer runTestCase(String testCopybookBasename, String expectedExpansionBasename) throws IOException {
        testCopybookFilename = testCopybookBasename + copybookFilenameSuffix;
        expectedResult = getExpectedResult(expectedExpansionBasename + copybookFilenameSuffix);
        Writer expandedSource = new StringWriter();
        expandedSource = copybookExpander.expand(
                expandedSource,
                testCopybookBasename,
                copybookFilenameSuffix);
        return expandedSource;
    }

    private String getExpectedResult(String copybookFilename) throws IOException {
        return Files.readString(Path.of(pathToTestCobolCopybooks + copybookFilename));
    }

    private static String getPathFor(String configPropertyName, String defaultValue) {
        String pathString = EMPTY_STRING;
        String directoryName =
                config.getString(configPropertyName,
                        "testcobolsources");
        if (directoryName.startsWith(FILE_SEPARATOR)) {
            pathString = directoryName;
        } else {
            pathString =
                    config.getString("resources.directory")
                            + FILE_SEPARATOR
                            + CopybookExpanderTest.class.getPackageName().replace(".", FILE_SEPARATOR)
                            + FILE_SEPARATOR
                            + directoryName
                            + FILE_SEPARATOR;
        }
        return pathString;
    }
}