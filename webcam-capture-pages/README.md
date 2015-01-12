# Webcam Capture Pages

This project is to generate the website.

### Update Site From README.md

To regenerate HTML from ```README.md``` file the following must be done:

```plain
$ cd webcam-capture-pages
$ mvn clean process-sources
```

After the Maven completed all its tasks, the newest website code is available in ```src/main/resources/html```.

### Upload File via FTP/SCP

Just use your tool of choice to deploy the site to the server.

