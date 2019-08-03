package me.aki.tactical.conversion.smali2dex.postprocessor;

import me.aki.tactical.dex.DexBody;

/**
 * Apply changes to a {@link DexBody}.
 */
public interface PostProcessor {
    void process(DexBody body);
}
