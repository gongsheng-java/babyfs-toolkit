package com.babyfs.tk.commons.application;

import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 使用Apache CLI {@link org.apache.commons.cli.Options} 解析命令行参数
 */
public abstract class CliOptionsApp extends ApplicationSupport {
    private static final Logger LOGGER = LoggerFactory.getLogger(CliOptionsApp.class);

    @Override
    public void init(String[] args) {
        super.init(args);
        Options options = options();
        CommandLineParser parser = new BasicParser();
        try {
            CommandLine commandLine = parser.parse(options, args);
            if (!checkOptions(commandLine)) {
                usage(options);
            }
            initWithCliOptions(commandLine, options);
        } catch (ParseException e) {
            LOGGER.error("Parse options error", e);
            usage(options);
        }
    }


    /**
     * @return
     */
    public abstract Options options();

    /**
     * @param commandLine
     * @return
     */
    public abstract boolean checkOptions(CommandLine commandLine);

    /**
     * 使用cli options 进行初始化
     *
     * @param commandLine
     * @param options
     */
    protected abstract void initWithCliOptions(CommandLine commandLine, Options options);

    /**
     * 输出Usage
     *
     * @param options
     */
    protected void usage(Options options) {
        HelpFormatter helpFormatter = new HelpFormatter();
        helpFormatter.printHelp(this.getClass().getName(), options);
        System.exit(1);
    }
}
