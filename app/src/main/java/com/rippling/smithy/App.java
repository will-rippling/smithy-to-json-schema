package com.rippling.smithy;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import net.sourceforge.argparse4j.ArgumentParsers;
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

        parser.addArgument("file").nargs("+")
                .help("Smithy schema file to convert.");

        Namespace ns = null;
        try {
            ns = parser.parseArgs(args);
        } catch (ArgumentParserException e) {
            parser.handleError(e);
            System.exit(1);
        }
        return ns;
    }


    public static void main(String[] args) {
        final Namespace ns = parseArgs(args);

        final List<String> files = ns.getList("file");
        if (files.isEmpty()) {
            System.err.println("No files provided.");
            System.exit(1);
        }

        final List<String> absoluteFiles = files.stream()
            .map(file -> file.replaceFirst("^~", System.getProperty("user.home")))
            .map(file -> Paths.get(file).toAbsolutePath())
            .map(Path::toString)
            .collect(Collectors.toList());

        System.out.println(absoluteFiles);

        for (String file : absoluteFiles) {
            final Model model = Model.assembler()
                    .addImport(file)
                    .disableValidation()
                    .putProperty(ALLOW_UNKNOWN_TRAITS, true)
                    .assemble()
                    .unwrap();
            SchemaDocument document = JsonSchemaConverter.builder().model(model).build().convert();
            Node node = document.toNode();
            System.out.println(Node.prettyPrintJson(node));
        }
    }
}
