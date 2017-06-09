# disposables (and pausables)
An experimental java library for dealing with objects that need to be disposed.

### Installataion
```groovy
repositories { maven { url "https://oss.sonatype.org/content/repositories/snapshots/" } }
dependencies {
    // core module
    compile 'com.episode6.hackit.disposable:disposables-core:0.0.2-SNAPSHOT'

    // pausables core module
    compile 'com.episode6.hackit.disposable:pausables-core:0.0.2-SNAPSHOT'

    // disposable support for listenable futures
    compile 'com.episode6.hackit.disposable:disposable-futures:0.0.2-SNAPSHOT'
}
```

### What / Why?
Note: While the examples below are all shown in Android, disposables is a pure Java library, and should be applicable anywhere Java7 is.

The goal of this project is to allow you (the developer) to couple your setup and tear-down logic for heavy-weight objects in one place. As well as help you avoid holding references purely so they may be cleaned up at some point.

At it's heart, a `Disposable` is a simple interface with a single method `void dispose();`. Over the lifetime of your app/component/lifecycle, you can continually add Disposables to a `DisposableManager`, and when it's time to tear town the app/component/lifecycle, simply call `manager.dispose()` to tear down everything in one method call.

Lets take a look at this simple Android Activity and see how disposables can help. Here you can see our activity holds references to 3 different object. All of them are created/setup in onCreate() and any one of them will trigger a memory leak if we forget to tear them down in onDestroy().
```java
// Without Disposables (pseuso-code)
public class MyActivity extends Activity {

  private SqlConnection mSqlConnection;
  private Service myService;
  private Service.Listener myServiceListener;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    mSqlConnection = SQL.openDb(/* db params */);

    myService = bindService(MyService.class);

    myServiceListener = new Service.Listener() {/* listener impl */};
    myService.registerListener(myServiceListener);
  }

  @Override
  protected void onDestroy() {
    if (myServiceListener != null) {
      myService.unregisterListener(myServiceListener);
    }

    if (myService != null) {
      myService.unbind();
    }

    if (mSqlConnection != null) {
      mSqlConnection.close();
    }
  }
}
```
I find the above approach problematic for a few reasons...
 - The tear-down code is not coupled with the setup code. Each of these components must be created in one method and torn-down in another (which could be several thousand lines away). This can be tricky to maintain and is easy to break (or leak). If you've ever used golang's `defer` command, you may understand my inspiration.
 - We're holding references to things we don't need. While our activity will probably interact with the sqlConnection, we're not interacting with the Service beyond registering and un-registering a listener. There's no good reason for us to hold these references.
 - The cleanup must be manually sorted which is easy to break. For example, if you accidentally added the myServiceListener cleanup block to the bottom of onDestroy(), instead of the top, it would throw a NullPointerException because myService was already unbound and cleaned up.

Now lets see what the same class looks like using Disposables to handle the tear-down...
```java
// With Disposables (pseuso-code)
public class MyActivity extends Activity {

  private final DisposableManager mDisposables = Disposables.newManager();

  private SqlConnection mSqlConnection;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    mSqlConnection = SQL.openDb(/* db params */);
    mDisposables.add(new Disposable() {
        public void dispose() {
            mSqlConnection.close();
        }
    });

    final myService = bindService(MyService.class);
    mDisposables.add(new Disposable() {
        public void dispose() {
            myService.unbind();
        }
    });

    final Service.Listener serviceListener = new Service.Listener() {/* listener impl */};
    myService.registerListener(serviceListener);
    mDisposables.add(new Disposable() {
        public void dispose() {
            myService.unregisterListener(serviceListener);
        }
    });
  }

  @Override
  protected void onDestroy() {
    mDisposables.dispose();
  }
}
```
At first glance, our new class may not seem much simpler, but for each of our components, we've now coupled our creation logic and our tear-down logic. In addition, we no longer need to hold explicit references to the Service or Service.Listener. We also know that the disposables we added will be disposed in reverse order, which guarantees our Service.Listener will be unregistered before we unbind our service.

Since we probably have lots of activities and services in our app that also need to create and dispose of these object types, we can clean this code up even further with a few static utility methods...
```java
public class MoreDisposables {
  public static Disposable forSqlConnection(final SqlConnection sqlConn) {
    return new Disposable() {
        public void dispose() {
            scqlConn.close();
        }
    };
  }

  public static Disposable forUnbindService(final Service service) {
    return new Disposable() {
        public void dispose() {
            service.unbind();
        }
    };
  }

  public static Disposable forUnregisterListener(
      final Service service,
      final Service.Listener listener) {
    return new Disposable() {
        public void dispose() {
            service.unregisterListener(listener);
        }
    };
  }
}
```

Then our activity looks more like this...
```java
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    mSqlConnection = SQL.openDb(/* db params */);
    mDisposables.add(MoreDisposables.forSqlConnection(mSqlConnection));

    myService = bindService(MyService.class);
    mDisposables.add(MoreDisposables.forUnbindService(myService));

    Service.Listener serviceListener = new Service.Listener() {/* listener impl */};
    myService.registerListener(serviceListener);
    mDisposables.add(MoreDisposables.forUnregisterListener(service, serviceListener));
  }

  @Override
  protected void onDestroy() {
    mDisposables.dispose();
  }
```

### Managing Memory
So far we've only created disposables for objects that are expected to exist for the entire life of our app/component/lifecycle, but you may want to use disposables to clean up temporary objects too. For this we've got `CheckedDisposable`, an extension to the Disposable interface that adds the method `boolean isDisposed()`. This allows a DisposableManager to occasionally flush references to CheckedDisposables if they are already disposed.

For an example of CheckedDisposable, consider an activity that shows a dialog when the user clicks a button. A dialog could be disposed by the user dismissing it, but if it's not and we leave it attached when the application exits, we've leaked memory. So lets create a CheckedDisposable for it.
```java
static CheckedDisposable dialogDisposable(final Dialog dialog) {
  return new CheckedDisposable() {
     public void dispose() {
         dialog.dismiss();
     }

     public boolean isDisposed() {
         return !dialog.isShowing();
     }
 };
}

public void onButtonClick() {
  Dialog dialog = new CustomInfoDialog();

  // add disposable for dialog
  mDisposables.add(dialogDisposable(dialog);

  dialog.show();
}

public void onTrimMemory() {
  // when and how often to call this method is up to you.
  mDisposables.flushDisposed();
}
```
With this code, we could wind up adding any number of CheckedDisposables to our disposable manager. But we'll be able to flush all the completed ones at any time by calling `mDisposables.flushDisposed()`. Note that when and how often to call `DisposableManager.flushDisposed()` is up to you and is highly dependent on how you use disposables in your project.

If you're an android developer you may have noticed a glaring problem with the dialog example above. In android a Dialog can actually be a fairly memory-heavy object since it contains it's own set of views, etc. And even though our activity isn't holding an explicit reference to each dialog, the disposable manager will still hold those strong references until `flushDisposed()` is called. In this case it would be preferable to use a weak disposable created by `Disposables.weak(T instance, Disposer<T> disposer)` which would allow our dialog to "fall out" of memory once no other objects hold strong references to it...
```java
static CheckedDisposable dialogDisposable(Dialog dialog) {
  // The returned disposable will hold a weak reference to dialog
  // instead of a strong one. The provided disposer will only be
  //called if the dialog is non-null.
  return Disposables.weak(
      dialog,
      new CheckedDisposer<Dialog> {
        public boolean isInstanceDisposed(Dialog instance) {
          return !instance.isShowing();
        }

        public void disposeInstance(Dialog instance) {
          instance.dismiss();
        }
      });
}
```

There is also a `HasDisposables` interface which also extends `Disposable` and adds the method `boolean flushDisposed()`. A DisposableManager will treat HasDisposables just like CheckedDisposables; if `flushDisposed()` returns true, the object is considered disposed and it's removed from the collection. The only real difference between `CheckedDisposable` and `HasDisposables` is the implied contract of what their respective methods do/don't do.

### Pausables
The `pausables-core` module is intended for application components that can be paused (like android activities/fragments). It adds support for `Pausable`, another simple interface with two methods...
```java
public interface Pausable {
  void pause();
  void resume();
}
```
Just like  disposables, you create Pausables at the same time you create the object-to-be-paused, and add them to a `PausableManager` that your component owns. When your component is paused or resumed, just call `PausableManager.pause()` or `PausableManager.resume()` respectively and the call will be passed down to all your pausables in correct order. The PausableManager also implements `HasDisposables`, so it acts similarly to DisposableManager and will flush / dispose of any pausables that happen to implement `Disposable`.

To create a PausableManager you have to options provided...
```java
  final PausableManager mPausables = Pausables.newStandaloneManager();
```
Or if you're pairing Pausables with Disposables
```java
  final DisposableManager mDisposables = Disposables.newManager();
  final PausableManager mPausables = Pausables.newConnectedManager(mDisposables);
```
The reason we use a connected PausableManager is to ensure all our Pausables and Disposables are disposed of in the correct order regardless of which collection they belong to. When using a connected PausableManager, you **do not** need to call dispose() or flushDisposed() directly on it. Calls to the DisposableManager will be properly propogated.

### Disposable Futures
The `disposable-futures` module adds support for `DisposableFuture<V>`, an extension of [guava](https://github.com/google/guava)'s `ListenableFuture<V>`. It works by implementing `HasDisposables` and maintaining its own internal collection of disposables. Whenever a listener is added to it, the Runnable is wrapped in a single-use DisposableRunnable and added to the internal collection before being added to the underlying future. This allows the DisposableFuture to effectively cancel all its callbacks upon disposal and release those references to avoid leaking them.

Let's take a look at an android activity using ListenableFutures and see how DisposableFutures can help. In this case we're assuming our Service provides a ListenableFuture for some api data that we want to display.

```java
public class MyActivity extends Activity {

  private final DisposableManager mDisposables = Disposables.newManager();

  private Executor mUiExecutor;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    // AndroidExecutors is a made up class, you should provide
    // your own executor
    mUiExecutor = AndroidExecutors.uiThreadExecutor();

    Service myService = bindService(MyService.class);
    mDisposables.add(MoreDisposables.forUnbindService(myService));

    // Fetch api data from myService and add a callback for it.
    ListenableFuture<SomeApiData> apiDataFuture = myService.getApiData();
    Futures.addCallback(
        apiDataFuture,
        new FutureCallback<SomeApiData> {
            public void onSuccess(SomeApiData result) {
              // showApiData(result);
            }

            public void onFailure(Throwable t) {
              // showLoadingError(t);
            }
        },
        mUiExecutor);
  }

  @Override
  protected void onDestroy() {
    mDisposables.dispose();
  }
}
```

This basic use of a ListenableFuture might look fine at first, especially because it will actually work. The problem is, until the future "returns" it's actually holding a strong reference to your activity by way of the FutureCallback. So if your activity is destroyed before the future returns, not only do you leak memory (since the activity can't be garbage collected) but the callback will still fire sometime after the activity is destroyed.

Let's correct these issues with a DisposableFuture...
```java
public class MyActivity extends Activity {

  private final DisposableManager mDisposables = Disposables.newManager();

  private Executor mUiExecutor;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    mUiExecutor = AndroidExecutors.uiThreadExecutor();

    Service myService = bindService(MyService.class);
    mDisposables.add(MoreDisposables.forUnbindService(myService));

    // wrap with a DisposableFuture before adding callbacks to it.
    DisposableFuture<SomeApiData> disposableFuture = DisposableFutures.wrap(myService.getApiData());

    // add callbacks to the DisposableFuture
    Futures.addCallback(
        disposableFuture,
        new FutureCallback<SomeApiData> {
            public void onSuccess(SomeApiData result) {
              // showApiData(result);
            }

            public void onFailure(Throwable t) {
              // showLoadingError(t);
            }
        },
        mUiExecutor);

    // Lastly, add the DisposableFuture to your DisposableManager.
    // Only do this once you've finished adding callbacks.
    mDisposables.add(disposableFuture);
  }

  @Override
  protected void onDestroy() {
    mDisposables.dispose();
  }
}
```
Because we're now adding our callback to a DisposableFuture (and disposing it in onDestroy, by-way of the DisposableManager), we can be certain that our callback will not fire after our activity is destroyed, and that the reference to our callback will be released (ensuring the activity can be garbage collected).

It's important to note that we added the DisposableFuture to DisposableManager **last**, after we'd already added our callback to it. This is vital because a DisposableFuture will become disposed if `flushDisposed()` is called and the underlying collection of disposables is/becomes empty.

Since the above example of wrapping a ListenableFuture, adding a callback, and registering the disposable can be very common, we provide the convenience method `DisposableFutures.addCallback(ListenableFuture, FutureCallback, Executor)` that returns a Disposable, so that all 3 tasks can be accomplished in one call.
```java
mDisposables.add(
    DisposableFutures.addCallback(
        myService.getApiData(),
        new FutureCallback<SomeApiData> {
            /* callback */
        },
        mUiExecutor));
```

Since all our examples have involved an android activity, there is one more optimization we should make. Even though we've prevented the callback from firing after our activity has been destroyed, it can still fire while the activity is off the screen, after it's been paused. To handle that we can simply wrap our uiExecutor with a `PausableExecutor` provided by the `pausables-core` module. The whole thing could look something like this...
```java
public class MyActivity extends Activity {

  private final DisposableManager mDisposables = Disposables.newManager();

  // note: PausableManager is kind of pointless in this example since we only have
  // a single Pausable to manage, but we leave it in as a usage example.
  private final PausableManager mPausables = Pausables.newConnectedManager(mDisposables);

  private PausableExecutor mUiExecutor;

  @Override
  protected void onCreate(Bundle savedInstanceState) {

    // A queuing pausable executor will collect a list of runnables while paused
    // and fire them upon resume
    mUiExecutor = Pausables.queuingExecutor(AndroidExecutors.uiThreadExecutor());
    mPausables.add(mUiExecutor);

    Service myService = bindService(MyService.class);
    mDisposables.add(MoreDisposables.forUnbindService(myService));

    mDisposables.add(
        DisposableFutures.addCallback(
            myService.getApiData(),
            new FutureCallback<SomeApiData> {
                public void onSuccess(SomeApiData result) {
                  // showApiData(result);
                }

                public void onFailure(Throwable t) {
                  // showLoadingError(t);
                }
            },
            mUiExecutor));
  }

  @Override
  protected void onPause() {
    mPausables.pause();
  }

  @Override
  protected void onResume() {
    mPausables.resume();
  }

  @Override
  protected void onDestroy() {
    mDisposables.dispose();
  }
}
```
If the activity is paused before the api's future "returns" the callback will now be queued, and then executed when the activity resumes (or released if the activity is destroyed).

See the [DisposableFutures](disposable-futures/src/main/java/com/episode6/hackit/disposable/future/DisposableFutures.java) class for more available utility methods for working with DisposableFutures.

### License
MIT: https://github.com/episode6/disposables/blob/master/LICENSE