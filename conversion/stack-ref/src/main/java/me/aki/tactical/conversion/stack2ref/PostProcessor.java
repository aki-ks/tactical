package me.aki.tactical.conversion.stack2ref;

import me.aki.tactical.ref.RefBody;

/**
 * Apply patches to an already build RefBody
 */
public interface PostProcessor {
    public void process(RefBody body);
}
