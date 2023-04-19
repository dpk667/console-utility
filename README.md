# console-utility

the task:
write a console utility that:
- o analyzes the exporters folder next to the exe on startup:
o analyzes the exporters folder next to the exe
o finds all the dll files in it, which are .Net libraries, and finds exporters in them
 exporter is a class that takes text as input and exports it. By export we mean, for example, saving it to a file, sending it by mail, sending an http request, etc.
o found exporters are registered for further use
- every minute:
o analyzes the input folder next to the exe, finds all *.txt files in it and in subfolders
o sorts them by name (if they have the same name, the order within the group is not important)
o reads the contents of all files in this order, merges them into one stream and exports them using all exporters found during the startup.
In addition, implement a test exporter, which saves the text in a unique file in the output folder next to the exe.

The advantage will be the design of the code with the assumption that:
- it will be finalized by other developers;
- it is intended for use in the production environment of the enterprise system.



As a result, an archive was generated with the code, without bin and obj folders and other artifacts of building and running.
