package io.setaicompanion.cli;

import java.net.URI;
import java.util.List;

/**
 * Parsed command-line options for a single batch run.
 *
 * @param configUri       URI to the JSON configuration file (sources + credentials)
 * @param stateUri        URI to the JSON state file (checkpoints)
 * @param storeImpl       explicit store provider name (null = auto-detect)
 * @param marshaller      explicit marshaller provider name (null = auto-detect)
 * @param agent           agent name: "claude" or "ibm-bob"
 * @param help            true when --help / -h was passed
 * @param configShow      print config file and exit
 * @param configAdd       options for --config-add
 * @param configRemove    [type, url] pair to remove from config
 * @param configFilter    [type, url, token...] for --config-filter; empty if not set
 * @param collectType     if set, run only this collector type
 * @param collectUrl      if set, run only this URL
 * @param overrideCheckpoint  if set, override the checkpoint before collecting
 */
public record CLIOptions(
    URI configUri,
    URI stateUri,
    String storeImpl,
    String marshaller,
    String agent,
    boolean help,
    boolean configShow,
    ConfigAddOptions configAdd,
    List<String> configRemove,
    List<String> configFilter,
    String collectType,
    String collectUrl,
    String overrideCheckpoint
) {
    public record ConfigAddOptions(
        String eventType,
        String eventUrl,
        String eventUser,
        String eventApiToken,
        String eventPassword
    ) {}
}
