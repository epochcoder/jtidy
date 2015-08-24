package org.w3c.tidy.tests;


import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import org.junit.Assert;
import static org.junit.Assert.assertNotNull;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.tidy.Tidy;
import org.w3c.tidy.TidyMessage;
import org.w3c.tidy.TidyMessageListener;

/**
 * tests if a file is read and parsed correctly
 * @author Willie Scholtz
 */
public class WordCharacterTest {

    private static final Logger LOG = LoggerFactory.getLogger(WordCharacterTest.class);
    private static final String TEST_FILE_NAME = "word-chars-test.html";

    private Tidy tidy;

    @Before
    public void setUp() {
        this.tidy = new Tidy();

        this.tidy.setTidyMark(false);
        this.tidy.setXHTML(true);

        // ensure input & output encodings are set
        this.tidy.setInputEncoding("UTF-8");
        this.tidy.setOutputEncoding("UTF-8");

        // debugging opts
        this.tidy.setShowWarnings(false);
        this.tidy.setQuiet(true);

        // ensure doctype
        this.tidy.setDocType("auto");

        this.tidy.setWord2000(true);
        this.tidy.setMakeBare(true);
        //this.tidy.setWraplen(0); // if wrapping is enabled it creates our issue.... disable by setting 0

        this.tidy.setShowWarnings(true);
        this.tidy.setSmartIndent(true);
        this.tidy.setMessageListener(new TidyMessageListener() {
            @Override
            public void messageReceived(TidyMessage message) {
                LOG.info("level={}, line={}, column={}, message={}",
                        message.getLevel(), message.getLine(),
                        message.getColumn(), message.getMessage());
            }
        });
    }

    @Test
    public void testFileExists() {
        assertNotNull("Test file missing", this.getClass()
                .getResource("/" + TEST_FILE_NAME));
    }

    @Test
    public void testParse() {
        ByteArrayOutputStream baos = null;
        InputStream is = null;
        try {
            is = this.getClass().getResourceAsStream("/" + TEST_FILE_NAME);
            baos = new ByteArrayOutputStream();

            this.tidy.parse(is, baos);
            baos.flush();

            final String result = baos.toString("UTF-8");

            LOG.info("got parsed result[{}]", result);

            assertNotNull("failed to parse test file["
                    + TEST_FILE_NAME + "]", result != null);

            // look for unicode NUL within the result, this makes DOM parsers fail
            final char NUL = 0x0;

            Assert.assertFalse("the parsed string contains unicode NUL["
                    + NUL + "], DOM parsers will fail!", result.indexOf(NUL) > -1);
        } catch (Exception e) {
            LOG.info("error while tidy-ing html!", e);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (Exception e) {}
            }

            if (baos != null) {
                try {
                    baos.close();
                } catch (Exception e) {}
            }
        }
    }
}
