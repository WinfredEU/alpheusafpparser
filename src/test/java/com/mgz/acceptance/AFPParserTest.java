/*
Copyright 2015 Rudolf Fiala

This file is part of Alpheus AFP Parser.

Alpheus AFP Parser is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Alpheus AFP Parser is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Alpheus AFP Parser.  If not, see <http://www.gnu.org/licenses/>
 */
package com.mgz.acceptance;

import com.mgz.afp.base.StructuredField;
import com.mgz.afp.base.StructuredFieldIntroducer;
import com.mgz.afp.parser.AFPParser;
import com.mgz.afp.parser.AFPParserConfiguration;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.DigestInputStream;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.util.Arrays;

import static org.junit.Assert.*;

@Ignore
public class AFPParserTest {
    private static File[] filesSuite = {};

    @BeforeClass
    public static void onlyOnce() throws Exception {
        filesSuite = FilesSuite.getAfpFiles();
        assertTrue("No AFP Testiles found", filesSuite != null && filesSuite.length > 0);
    }

    @Test
    public void testParsingAllTestFiles() throws Exception {
        AFPParserConfiguration pc = new AFPParserConfiguration();
        for (File afpFile : filesSuite) {

            pc.setInputStream(new FileInputStream(afpFile));

            AFPParser parser = new AFPParser(pc);

            StructuredField sf;
            do {
                sf = parser.parseNextSF();
                if (sf != null) {
                    StructuredFieldIntroducer sfi = sf.getStructuredFieldIntroducer();
                    assertNotNull(sfi);
                }
            } while (sf != null);

            pc.getInputStream().close();
        }
    }

    @Test
    public void testAFPSerializationActualClassType() throws Exception {
        AFPParserConfiguration pc = new AFPParserConfiguration();
        pc.setParseToStructuredFieldsBaseData(false);
        File tmpFile = new File("junit_testWritingTmp.afp");

        MessageDigest mdIs = MessageDigest.getInstance("MD5");
        MessageDigest mdOs = MessageDigest.getInstance("MD5");

        for (File afpFile : filesSuite) {
            DigestInputStream dis = null;
            DigestOutputStream dos = null;
            try {
                dis = new DigestInputStream(new FileInputStream(afpFile), mdIs);
                pc.setInputStream(dis);

                dos = new DigestOutputStream(new FileOutputStream(tmpFile), mdOs);

                AFPParser parser = new AFPParser(pc);

                StructuredField sf;
                do {
                    sf = parser.parseNextSF();
                    if (sf != null) {
                        sf.writeAFP(dos, pc);

                        assertArrayEquals(afpFile.getName() + " 0x" + Long.toHexString(sf.getStructuredFieldIntroducer().getFileOffset()) + " " + sf.getClass().getSimpleName(),
                                dis.getMessageDigest().digest(),
                                dos.getMessageDigest().digest()
                        );

                    }

                } while (sf != null);
            } finally {
                if (dos != null) dos.close();
                if (dis != null) dis.close();
                assertTrue("Failed to delete tmp file " + tmpFile.getAbsolutePath() + " after test finished.", tmpFile.delete());
            }
        }
    }

    @Test
    public void testAFPSerializationStructuredFieldBase() throws Exception {
        AFPParserConfiguration pc = new AFPParserConfiguration();
        pc.setParseToStructuredFieldsBaseData(true);
        File tmpFile = new File("junit_testWritingTmp.afp");

        MessageDigest mdIs = MessageDigest.getInstance("MD5");
        MessageDigest mdOs = MessageDigest.getInstance("MD5");

        for (File afpFile : filesSuite) {

            DigestInputStream dis = null;
            DigestOutputStream dos = null;
            try {

                dis = new DigestInputStream(new FileInputStream(afpFile), mdIs);
                pc.setInputStream(dis);

                dos = new DigestOutputStream(new FileOutputStream(tmpFile), mdOs);

                AFPParser parser = new AFPParser(pc);

                StructuredField sf;
                do {
                    sf = parser.parseNextSF();
                    if (sf != null) {
                        sf.writeAFP(dos, pc);
                        if (!Arrays.equals(mdIs.digest(), mdOs.digest())) {

                            sf.writeAFP(dos, pc);
                            assertArrayEquals(afpFile.getName() + " 0x" + Long.toHexString(sf.getStructuredFieldIntroducer().getFileOffset()) + " " + sf.getClass().getSimpleName(),
                                    dis.getMessageDigest().digest(),
                                    dos.getMessageDigest().digest()
                            );
                        }

                    }

                } while (sf != null);

            } finally {
                if (dos != null) dos.close();
                if (dis != null) dis.close();
                assertTrue("Failed to delete tmp file " + tmpFile.getAbsolutePath() + " after test finished.", tmpFile.delete());
            }
        }
    }
}
