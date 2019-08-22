# MiniTrello

Projeto final da disciplina de Sistemas Distribuídos 
realizada em 2019.2 na UFABC.

## Executando

Primeiramente, configure o ZooKeeper na sua máquina, baixando a versão
3.4.14 e descompactando em uma pasta de preferência sua. Também é necessário
possuir o [Gradle] instalado e configurado para efetuar a compilação.

Para executar, siga os comandos abaixo.

```console
Inicia o servidor do ZooKeeper.
$ bin/zkCli.sh start
Executa o aplicativo.
$ gradle run
Executa o aplicativo a partir do jar.
$ gradle runJar
```

Alternativamente, você pode querer ler os *logs* do ZooKeeper enquanto
usa a aplicação. Para isto, utilize o comando abaixo para executar.

```console
$ ./run.sh
```

[Gradle]: https://gradle.org/

## Contribuindo

Este repositório possui um guia de contribuição disponível [aqui].
Por favor, **siga-o estritamente**.

[aqui]: CONTRIBUTING.md

## Licença

> Você pode checar a licença completa [aqui](LICENSE).

Este repositório está licenciado pelos termos da licença **MIT**.
