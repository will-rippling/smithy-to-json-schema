package com.rippling.smithy;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import software.amazon.smithy.jsonschema.JsonSchemaConverter;
import software.amazon.smithy.jsonschema.SchemaDocument;
import software.amazon.smithy.model.Model;
import static software.amazon.smithy.model.loader.ModelAssembler.ALLOW_UNKNOWN_TRAITS;
import software.amazon.smithy.model.node.Node;

public class App {
    public String getGreeting() {
        return "Hello World!";
    }

    public static Namespace parseArgs(String[] args) {
        final ArgumentParser parser = ArgumentParsers.newFor("smithy-to-jsonschema").build()
                .defaultHelp(true)
                .description("Convert Smithy schema to JSON Schema.");

        parser.addArgument("-i", "--input").nargs("+")
                .type(Arguments.fileType().verifyCanRead().acceptSystemIn())
                .setDefault("-")
                .help("Input file or directory to convert.");

        parser.addArgument("-o", "--output")
                .setDefault("out")
                .type(Arguments.fileType()
                    .verifyNotExists().verifyCanCreate()
                    .or().verifyIsDirectory().verifyCanWrite()
                )
                .help("Output directory to write the JSON Schema to.");

        Namespace ns = null;
        try {
            ns = parser.parseArgs(args);
        } catch (ArgumentParserException e) {
            parser.handleError(e);
            System.exit(1);
        }
        return ns;
    }

    public static String convertModel(final File file) {
        final Model model = Model.assembler()
                .addImport(file.toString())
                .disableValidation()
                .putProperty(ALLOW_UNKNOWN_TRAITS, true)
                .assemble()
                .unwrap();

        SchemaDocument document = JsonSchemaConverter.builder().model(model).build().convert();
        Node node = document.toNode();
        return Node.prettyPrintJson(node);
    }

    public static void writeJson(final File output, final String json) throws IOException {
        try (final FileWriter writer = new FileWriter(output)) {
            writer.write(json);
        }
    }

    public static void main(String[] args) throws IOException {
        final Namespace ns = parseArgs(args);

        final List<File> files = ns.getList("input");
        if (files.isEmpty()) {
            System.err.println("No files provided.");
            System.exit(1);
        }
        final File output = ns.get("output");
        final Boolean _madeDirectories = output.mkdirs();


        for (final File file : files) {
            final String json = convertModel(file);
            writeJson(new File(output, file.getName()), json);
        }
    }
}
