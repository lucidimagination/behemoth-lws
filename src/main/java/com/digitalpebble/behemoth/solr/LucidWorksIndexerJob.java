/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.digitalpebble.behemoth.solr;

import java.util.Random;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.FileInputFormat;
import org.apache.hadoop.mapred.FileOutputFormat;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.SequenceFileInputFormat;
import org.apache.hadoop.mapred.lib.IdentityMapper;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import com.digitalpebble.behemoth.BehemothConfiguration;
import com.digitalpebble.behemoth.BehemothDocument;

/**
 * Sends annotated documents to LucidWorks for indexing
 */

public class LucidWorksIndexerJob extends Configured implements Tool {
    private static final Log LOG = LogFactory.getLog(LucidWorksIndexerJob.class);

    public LucidWorksIndexerJob() {
    }

    public LucidWorksIndexerJob(Configuration conf) {
        super(conf);
    }

    public static void main(String args[]) throws Exception {
        int res = ToolRunner.run(BehemothConfiguration.create(),
                new LucidWorksIndexerJob(), args);
        System.exit(res);
    }

    public int run(String[] args) throws Exception {

        final FileSystem fs = FileSystem.get(getConf());
        
        // Following behemoth style of printing help messages
        Options options = new Options();
        HelpFormatter formatter = new HelpFormatter();
        CommandLineParser parser = new GnuParser();
        
        options.addOption("help", false, "print this message");
        options.addOption("i", "input", true, "input behemoth corpus");
        options.addOption("o", "output", true, "Solr URL");
        
        Path inputPath = null;
        String solrURL = null;
        
        // parse the command line arguments
        CommandLine cmdLine = null;
        try {
            cmdLine = parser.parse(options, args);
            String input = cmdLine.getOptionValue("i");
            String solrUrl = cmdLine.getOptionValue("o");
            if (cmdLine.hasOption("help")) {
                formatter.printHelp("LucidWorksIndexerJob", options);
                return 0;
            }
            if (input == null | solrUrl == null) {
                formatter.printHelp("LucidWorksIndexerJob", options);
                return -1;
            }
            inputPath = new Path(input);
            solrURL = new String(solrUrl);
        } catch (ParseException e) {
            formatter.printHelp("LucidWorksIndexerJob", options);
        }

        JobConf job = new JobConf(getConf());

        job.setJarByClass(this.getClass());

        job.setJobName("Indexing " + inputPath + " into LucidWorks");

        job.setInputFormat(SequenceFileInputFormat.class);
        job.setOutputFormat(LucidWorksOutputFormat.class);

        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(BehemothDocument.class);

        job.setMapperClass(IdentityMapper.class);
        // no reducer : send straight to SOLR at end of mapping
        job.setNumReduceTasks(0);

        FileInputFormat.addInputPath(job, inputPath);
        final Path tmp = new Path("tmp_" + System.currentTimeMillis() + "-"
                + new Random().nextInt());
        FileOutputFormat.setOutputPath(job, tmp);

        job.set("solr.server.url", solrURL);

        try {
          long start = System.currentTimeMillis();
          JobClient.runJob(job);
          long finish = System.currentTimeMillis();
          if (LOG.isInfoEnabled()) {
            LOG.info("LucidWorksIndexerJob completed. Time " + (finish - start) + " ms");
          }
        } catch (Exception e) {
            LOG.error(e);
        } finally {
            fs.delete(tmp, true);
        }

        return 0;
    }
}
