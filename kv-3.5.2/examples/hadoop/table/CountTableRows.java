/*-
 *
 *  This file is part of Oracle NoSQL Database
 *  Copyright (C) 2011, 2015 Oracle and/or its affiliates.  All rights reserved.
 *
 *  Oracle NoSQL Database is free software: you can redistribute it and/or
 *  modify it under the terms of the GNU Affero General Public License
 *  as published by the Free Software Foundation, version 3.
 *
 *  Oracle NoSQL Database is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public
 *  License in the LICENSE file along with Oracle NoSQL Database.  If not,
 *  see <http://www.gnu.org/licenses/>.
 *
 *  An active Oracle commercial licensing agreement for this product
 *  supercedes this license.
 *
 *  For more information please contact:
 *
 *  Vice President Legal, Development
 *  Oracle America, Inc.
 *  5OP-10
 *  500 Oracle Parkway
 *  Redwood Shores, CA 94065
 *
 *  or
 *
 *  berkeleydb-info_us@oracle.com
 *
 *  [This line intentionally left blank.]
 *  [This line intentionally left blank.]
 *  [This line intentionally left blank.]
 *  [This line intentionally left blank.]
 *  [This line intentionally left blank.]
 *  [This line intentionally left blank.]
 *  EOF
 *
 */

package hadoop.table;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import oracle.kv.ParamConstant;
import oracle.kv.hadoop.table.TableInputFormat;
import oracle.kv.table.PrimaryKey;
import oracle.kv.table.Row;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.mapreduce.lib.reduce.IntSumReducer;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

/**
 * A basic example demonstrating how to use the class
 * <a href="../../../javadoc/oracle/kv/hadoop/table/TableInputFormat.html"><b><code>oracle.kv.hadoop.table.TableInputFormat</code></b></a> to access the
 * rows of a table in an Oracle NoSQL Database from within a Hadoop
 * MapReduce job for the purpose of counting the number of records
 * in the table.
 * <p>
 * The <code>map</code> function is passed the PrimaryKey and Row for each
 * record in the KV Store and outputs key/value pairs containing the component
 * field names of the PrimaryKey as the output key and a value of 1. The
 * reduce phase then uses the output of the map phase to count the number
 * of records in the table. This MapReduce task is similar to the ubiquitous
 * Hadoop MapReduce WordCount example.
 * <p>
 * The <code>TableInputFormat</code> and related classes are located in the
 * <code>lib/kvclient.jar</code> file, so kvclient.jar must be included in
 * the Hadoop <em>libjars</em> classpath at runtime. Additionally, this class
 * and any dependent classes, must be placed in a separate JAR for inclusion
 * in the classpath of the application itself. Finally, if this example is to
 * be run against a secure store, then a third JAR file must be generated that
 * contains the user's login file, credentials, and trust store. For the
 * details on how to run this example class, both in a secure and a non-secure
 * mode, refer to the information presented in the
 * <a href="./package-summary.html#count_table_rows_example"><b><code>package summary</code></b></a> for this example.
 */
public class CountTableRows extends Configured implements Tool {

    private static final Class<?> THIS_CLASS = CountTableRows.class;
    private static final String THIS_CLASS_NAME = THIS_CLASS.getName();

    /*
     * The logger employed by the frontend client of the MapReduce job
     * initiated by this example class (the class that encapsulates the
     * MapReduce job's Mapper task and Reducer task). Generally, for
     * example purposes, one would configure this logger to write its
     * output to the console. Compare this logger with the logger defined
     * below, in the Map nested class.
     */
    private static Logger logger;

    /**
     * Nested class used to map input key/value pairs to a set of
     * intermediate key/value pairs in preparation for the reduce phase
     * of the associated MapReduce job. The Hadoop MapReduce framework
     * spawns one task -- running an instance of this class -- for each
     * <a href="../../../javadoc/oracle/kv/hadoop/table/TableInputSplit.html"><b><code>TableInputSlit</code></b></a>
     * generated by the
     * <a href="../../../javadoc/oracle/kv/hadoop/table/TableInputFormat.html"><b><code>TableInputFormat</code></b></a> employed by the job.
     */
    public static class Map
        extends Mapper<PrimaryKey, Row, Text, IntWritable> {

        /*
         * Logger for logging output from each Mapper task of the MapReduce
         * job that is executed. This logger must be different from the logger
         * employed by the job's frontend client class (the encapsulating
         * class of this Map task); not only because the logger in the
         * encapsulating class is not in the scope of this class, but also
         * because this logger is an Apache Commons logger, whereas the
         * logger employed by the encapsulating class is a java.util.logging
         * logger.
         *
         * Note that because the Mapper task(s) run on the Hadoop cluster's
         * DataNodes, the output of this logger is typically written to the
         * file system of the DataNode on which the associated Mapper task
         * executes. For example, if the Hadoop DataNodes are configured
         * to log to the directory /var/log/hadoop/node_manager/logs, then
         * under that directory, one would see additional directories and
         * files of the form:<br>
         * <code>
         *   /var/log/hadoop/node_manager/logs
         *     /application_1409172332346_0088
         *       /container_1409172332346_0088_01_000005
         *         /stderr
         *         /stdout
         *         /syslog
         * </code><br>
         * where the output of this logger is written to the syslog file;
         * and output for calls to System.err.println and System.out.println
         * is written to the stderr and stdout files respectively.
         */
        private static final Log MAP_TASK_LOGGER =
                                     LogFactory.getLog(THIS_CLASS_NAME);

        private Text word = new Text();
        private static final IntWritable one = new IntWritable(1);

        @Override
        public void map(PrimaryKey keyArg, Row valueArg, Context context)
            throws IOException, InterruptedException {

            /*
             * keyArg is a NoSQL Databse PrimaryKey.
             *
             * The Output is the Text object consisting of the component
             * field names of the PrimaryKey, concatenated and delimited
             * by "/"; which is the Map/Reduce key and 1 as the Map/Reduce
             * value.
             */
            final List<String> keyFieldComponents = keyArg.getFields();
            MAP_TASK_LOGGER.info(
                "PrimaryKey field components: " + keyFieldComponents);
            final StringBuilder wordBuf = new StringBuilder();
            for (String keyFieldComponent : keyFieldComponents) {
                wordBuf.append("/" + keyFieldComponent);
            }
            word.set(wordBuf.toString());
            MAP_TASK_LOGGER.info("MapReduce key: " + word);
            context.write(word, one);
        }
    }

    /**
     * Nested class used to reduce to a smaller set of values, the set of
     * intermediate values, sharing a key, which were produced by the
     * mapping phase of the job.
     */
    public static class Reduce
        extends IntSumReducer<Text> {
    }

    @Override
    @SuppressWarnings("deprecation")
    public int run(String[] args)
        throws Exception {

        final Configuration conf = getConf();

        /*
         * Instantiate the logger using the configuration info (if any)
         * specified on the command line. The 'hadoop' command line
         * interpreter places the values of the desired system properties
         * in the Configuration object above, not the system properties on
         * the JVM. As a result, because the logger retrieves its config
         * from the JVM's system properties (not the MapReduce Configuration),
         * the desired logger can be instantiated only after the necessary
         * logger config values are retrieved from the MapReduce config
         * and placed in the system properties of the JVM.
         *
         * Note that once the logger configuration system properties are set
         * on the JVM, they are available to all other loggers that may be
         * created by classes instantiated in the JVM; for example, any
         * utility classes employed by this class.
         */
        if (conf != null) {
            final String loggingConfig =
                             conf.get("java.util.logging.config.file");
            if (loggingConfig != null) {
                System.setProperty(
                    "java.util.logging.config.file", loggingConfig);
            }

            final String loggingFormat =
                conf.get("java.util.logging.SimpleFormatter.format");
            if (loggingFormat != null) {
                System.setProperty(
                    "java.util.logging.SimpleFormatter.format", loggingFormat);
            }
        }

        /* Since the JVM properties are set, the logger can now be created. */
        logger = Logger.getLogger(THIS_CLASS_NAME);

        final Job job = new Job(conf);
        job.setJarByClass(CountTableRows.class);
        job.setJobName("Count Table Rows");

        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);

        job.setMapperClass(Map.class);
        job.setReducerClass(Reduce.class);

        job.setInputFormatClass(TableInputFormat.class);
        job.setOutputFormatClass(TextOutputFormat.class);

        TableInputFormat.setKVStoreName(args[0]);
        TableInputFormat.setKVHelperHosts(new String[] { args[1] });
        TableInputFormat.setTableName(args[2]);

        FileOutputFormat.setOutputPath(job, new Path(args[3]));

        /*
         * Handle the optional client-side security information. If
         * accessing a secure store, then this information must be
         * specified using the last 2 arguments input on the command
         * line; where,
         * args[4] = name of the client side login file
         * args[5] = name of the server side login file
         */
        if (args.length >= 6) {
            logger.info("using a SECURE KVStore [" + args[4] +
                        ", " + args[5] + "]");
            KVSecurityUtil.createLocalKVSecurity(args[4], args[5]);
            TableInputFormat.setKVSecurity(
                                 KVSecurityUtil.getServerLoginFlnm(),
                                 KVSecurityUtil.getPasswordCredentials(),
                                 KVSecurityUtil.getClientTrustFlnm());
        } else {
            logger.info("using a NON-SECURE KVStore");
        }

        if (conf != null) {
            final String primaryKeyProperty =
                         conf.get(ParamConstant.PRIMARY_KEY.getName());
            if (primaryKeyProperty != null) {
                TableInputFormat.setPrimaryKeyProperty(primaryKeyProperty);
            }
        }

        final boolean success = job.waitForCompletion(true);
        return success ? 0 : 1;
    }

    public static void main(String[] args)
        throws Exception {

        final int ret = ToolRunner.run(new CountTableRows(), args);
        System.exit(ret);
    }
}
