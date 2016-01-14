package org.gooru.nucleus.handlers.courses.bootstrap.shutdown;

import org.gooru.nucleus.handlers.courses.app.components.DataSourceRegistry;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Finalizers implements Iterable<Finalizer> {


  private final Iterator<Finalizer> internalIterator;
  private List<Finalizer> finalizers = null;

  public Finalizers() {
    finalizers = new ArrayList<>();
    finalizers.add(DataSourceRegistry.getInstance());
    internalIterator = finalizers.iterator();
  }

  @Override
  public Iterator<Finalizer> iterator() {
    return new Iterator<Finalizer>() {

      @Override
      public boolean hasNext() {
        return internalIterator.hasNext();
      }

      @Override
      public Finalizer next() {
        return internalIterator.next();
      }

    };
  }


}
