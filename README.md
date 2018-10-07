# scoverage-diff

Simple tool to compare scoverage reports, inspired by pycobertura.

## Usage

For now, build locally with `sbt assembly`. Execute `java -jar target/scala-2.12/scoverage-diff-assembly-0.1.jar origin/scoverage.xml updated/scoverage.xml`.

The output will be a single line with exit code `0` if the coverage was equal or increased. E.g.

```
Global Coverage was **91.76** and now is **93.50**.
```

If coverage was degraded the tool prints the packages and classes that are the cause of this.

```
Global Coverage was **93.50** and now is **91.76**.

Overview of degredation per package:

| package name                        | previous coverage | current coverage |
| ----------------------------------- | ----------------- | ---------------- |
| com.foo.barbaz.package.cryptography | 94.71             | 58.20            |

Overview of degredation per class:

| class name                                               | previous coverage | current coverage |
| -------------------------------------------------------- | ----------------- | ---------------- |
| com.foo.barbaz.package.cryptography.FooBarEncryptionUtil | 90.00             | 67.14            |
| com.foo.barbaz.package.cryptography.EncryptionUtil       | 97.48             | 52.94            |
```

The tables are in a format supported by most Markdown engines. This allows for convenient reporting in merge/pull request comments.