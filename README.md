# disposables
An experimental java library for dealing with objects that need to be disposed.

### Installataion
```groovy
repositories { maven { url "https://oss.sonatype.org/content/repositories/snapshots/" } }
dependencies {
    // core module
    compile 'com.episode6.hackit.disposable:disposables-core:0.0.1-SNAPSHOT'

    // disposable support for listenable futures
    compile 'com.episode6.hackit.disposable:disposable-futures:0.0.1-SNAPSHOT'
}
```

### What / Why?
Disposables is a concept I've found myself coming back to a couple of times now, so I figured I'd try to formalize it. The goal of this project is to allow you (the developer) to couple your setup and tear-down logic for heavy-weight objects/services in one place. As well as help you avoid holding references to objects purely so they may be cleaned up at some point.

At it's heart, a `Disposable` is a simple interface with a single method `void dispose();`. Over the lifetime of your app/component/lifecycle, you can continually add Disposables to a `RootDisposableCollection`, and when it's time to tear town the app/component/lifecycle, simply call `collection.dispose()` to tear down everything in one method call.

Lets take a look at this simple Android Activity and see how disposables can help. Here you can see our activity holds references to a SqlConnection, a Service and a Service.Listener. All of them are created/setup in onCreate() and any one of them will trigger a memory leak if we forget to tear them down in onDestroy().
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
      myServiceListener = null;
    }

    if (myService != null) {
      myService.unbind();
      myService = null;
    }

    if (mSqlConnection != null) {
      mSqlConnection.close();
      mSqlConnection = null;
    }
  }
}
```
I find the above approach problematic for a few reasons...
 - The tear-down code is not coupled with the setup code. Each of these components must be created in one method and torn-down in another (which could be several thousand lines away). If you've ever used (and loved) golang's `defer` command, you should understand where I'm coming from.
 - We're holding references to things we don't need. While our activity will probably interact with the sqlConnection, we're not interacting with the Service beyond registering and un-registering a listener. There's no good reason for us to hold these references.
 - The cleanup must be manually sorted which is easy to break. For example, if you accidentally added the myServiceListener cleanup block to the bottom of onDestroy(), instead of the top, it would throw a NullPointerException because myService was already unbound and cleaned up.

Now lets see what the same class looks like using Disposables to handle the tear-down...
```java
// With Disposables (pseuso-code)
public class MyActivity extends Activity {

  private final RootDisposableCollection mDisposables = RootDisposableCollection.create();

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
