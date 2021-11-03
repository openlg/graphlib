package io.github.openlg.graphlib;

/**
 * @author lg
 * Create by lg on 4/27/20 8:05 AM
 */
public class Utils {

	/**
	 * Check whether the given object (possibly a {@code String}) is empty.
	 * @param str
	 * @return
	 */
	public static boolean isEmpty(String str) {
		return str == null || str.length() == 0;
	}
}
