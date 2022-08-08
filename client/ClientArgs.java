package client;

import com.beust.jcommander.Parameter;

import java.util.ArrayList;
import java.util.List;

public class ClientArgs {
    @Parameter
    List<String> parameters = new ArrayList<>();

    @Parameter(names = { "-t" }, description = "Type of command")
    String commandType;

    @Parameter(names = "-k", description = "Key of cell to use")
    String commmandKey;

    @Parameter(names = "-v", description = "Text to store in cell")
    String commandValue;

    @Parameter(names = "-in", description = "File to read command from")
    String commandFile;
}

