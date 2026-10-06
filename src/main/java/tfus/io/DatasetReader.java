package tfus.io;

import tfus.domain.MiningInput;

/** Reads dataset and config from a source and produces a MiningInput. */
public interface DatasetReader {
    MiningInput read(String source) throws Exception;
}
