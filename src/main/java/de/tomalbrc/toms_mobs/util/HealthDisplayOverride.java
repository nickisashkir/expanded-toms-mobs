package de.tomalbrc.toms_mobs.util;

/**
 * Entities that render much taller than their collision bbox can implement this to control
 * the vertical offset of the floating health hearts that {@link HealthDisplayHelper} shows.
 * Returned value is in blocks above the entity's feet/origin.
 */
public interface HealthDisplayOverride {
    double getHealthDisplayYOffset();
}
