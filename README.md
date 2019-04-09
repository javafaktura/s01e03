```
███╗   ███╗██╗   ██╗██╗     ████████╗██╗
████╗ ████║██║   ██║██║     ╚══██╔══╝██║
██╔████╔██║██║   ██║██║        ██║   ██║█████╗
██║╚██╔╝██║██║   ██║██║        ██║   ██║╚════╝
██║ ╚═╝ ██║╚██████╔╝███████╗   ██║   ██║
╚═╝     ╚═╝ ╚═════╝ ╚══════╝   ╚═╝   ╚═╝

████████╗██╗  ██╗██████╗ ███████╗ █████╗ ██████╗ ██╗███╗   ██╗ ██████╗
╚══██╔══╝██║  ██║██╔══██╗██╔════╝██╔══██╗██╔══██╗██║████╗  ██║██╔════╝
   ██║   ███████║██████╔╝█████╗  ███████║██║  ██║██║██╔██╗ ██║██║  ███╗
   ██║   ██╔══██║██╔══██╗██╔══╝  ██╔══██║██║  ██║██║██║╚██╗██║██║   ██║
   ██║   ██║  ██║██║  ██║███████╗██║  ██║██████╔╝██║██║ ╚████║╚██████╔╝
   ╚═╝   ╚═╝  ╚═╝╚═╝  ╚═╝╚══════╝╚═╝  ╚═╝╚═════╝ ╚═╝╚═╝  ╚═══╝ ╚═════╝
```

# s01e03

This is the 3rd episode of the *"Java is the new Black"* series.

The code you're about to read shows and explains basic concepts of multithreading in Java.

Functionally, each test is supposed to find the best (lowest)
PLN-EUR exchange rate by querying a remote REST service and comparing results for different dates.

You can use the [OFFLINE_MODE](src/test/java/io/github/javafaktura/s01/e03/RatesTestSupport.java#L27)
flag to switch tests between actual `https://api.exchangeratesapi.io` endpoint and a local mock.
