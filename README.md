# mesto

MEmory STOrage

## About

Mesto is a in-memory only storage, intended for use in Rich Browser
Applications.


## Usage

### Basic

This is fairly simple `assoc-in` call, identical to one from `clojure.core` (but
only operating on an inner storage of Mesto).

    (y/assoc-in [:countries :ukraine] {:capital "Kyiv" :location "Europe"})

It behaves like you expect it to behave, just sets a value by some path. But
here is a thing - you often have not a map, but a list of items, like that:

    (y/assoc-in [:countries]
                [{:name "Ukraine" :capital "Kyiv"}
                 {:name "Croatia" :capital "Zagreb"}])

In this case, addressing them becomes harder. You're not going to have this
list, render it somehow, and let it flow, as it often happens in back-end
applications, you're going to live with it, and possibly with changed version of
it. What if it is going to be sorted in different way? What if you get more
items appended/inserted? In this case, you have improved way to address an item:

    (y/get-in [:countries {:name "Ukraine"}])

Having a map in your path has special meaning - it's going to be treated like a
filter. Such a call will get you an item from `[:countries]` list, which
conforms to given filter. "Conforms" means "all matching keys from item should
have same value as in filter".

And here is the thing:

    (y/assoc-in [:countries {:name "Ukraine"} :location] "Europe")

This will result in adding `:location` key to "Ukraine" item.

### Notifications

When building an interface, it's good to have interface which will react to
change of data. For this, there is an `on` function, which notifies given
handler when some changes appear inside of path:

    (y/on [:countries {:name "Ukraine"}]
          (fn [path value] (log "path:" path "value:" value)))

Then updating anything in "Ukraine" item (or removing the item) will result in
handler being called.


## API specification

- `(assoc-in [:items {:id 1} :name] "something")`

  **NEEDS DECISION**

  Puts "x" in item, found by filter `{:id 1}`. If item does not exist, creates
  it based off filter and appends it in a sequence. If sequence does not exist,
  creates a vector.

  Or! Puts "x" in item, found by filter `{:id 1}`. If filter find no match,
  nothing happens. Creates items only by using literal paths, any "filters" will
  prevent creation of new elements. Makes it possible to have arbitrary
  functions as filters

- `(update-in [:items {:id 1}] fn)`

  Works like `assoc-in`.

- `(all-in [:items {:id 1} :name])`

  Returns all the items matched by path.

- `(get-in [:items {:id 1} :name])`

  Returns first item matched by path.

- `(on [items {:id 1}] (fn [path value] (log path value)))`

  **NOT IMPLEMENTED**

  Notifies when change occurs in path (i.e. if changes appear at or inside
  whatever happens to be matched by given path). `path` argument holds full path
  to changed element, and `value` holds value of an item matched by path
  supplied to `on` (not actually changed value).
