Admin UI Structure
==================

This is intended as a developer/contributor guideline for the Gyrex
Admin UI.


Navigation / Categories
-----------------------

The structure of the Admin UI is fixed. Please do not add new categories
without discussing the change with the usability experts on the dev team first.
We intentionally trimmed down the number of categories to simplify the structure
and to also separate the different administration aspects of a system.

It's also intentional that the new structure is only two-levels
(categories and pages). This should scale pretty well until the number of pages
explodes. Therefore, please also do not introduce new pages freely. It must be
discussed with the dev team whether the intent satisfy a new page and where it
should appear.



System Category
---------------

The System category is intended for system relevant settings that have no connection
to a context. This also includes setting up the contexts themselves.


Applications
------------

The Application category is intended for all administrative tasks associated with
applications. The current idea is to group the web/http application stuff as well
as background jobs (schedule configuration) under this category.


Data
----

The data category is intended for repositories, repository management, etc.
