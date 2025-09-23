package io.github.honhimw.jddl;

import java.util.List;

/**
 * @author honhimW
 */

public interface Exporter<T> {

    List<String> getSqlCreateStrings(T exportable);

    List<String> getSqlDropStrings(T exportable);

}
