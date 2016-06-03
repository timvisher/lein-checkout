### Example with lein-sub

This project is an example of using lein-sub and lein-checkout.

Add desired version of `lein-checkout` to your lein plugins.
* e.g. `[lein-checkout "0.4.2"]`


The expected behavior is as follows:
* `cd example-with-sub`

* `lein sub checkout ln`
  * This should create symlinks between the sub-projects
  ```bash
  $ ls */checkouts
  project-a/checkouts:
  project-b

  project-b/checkouts:
  project-c
  ```

* `lein sub checkout rm`
  * This should remove the symlinks between the sub-projects
  ```bash
  $ ls */checkouts
  project-a/checkouts:

  project-b/checkouts:
  ```
