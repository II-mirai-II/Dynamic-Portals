# Dynamic Portals Mob Head Assets

Este guia explica como criar imagens customizadas para os requisitos de kill exibidos no Hub do Dynamic Portals.

## Objetivo

No modal `Details` da aba `Progress`, requisitos de kill sao exibidos como:

```text
[imagem da cabeca] Nome do mob  contador
```

Exemplo visual:

```text
[Zombie head] Zombie  0/1
```

As imagens sao opcionais. Se uma imagem nao existir, o Hub mostra um fallback visual simples com a inicial do mob.

## Formato Da Imagem

- Formato obrigatorio: PNG.
- Resolucao recomendada: `32x32`.
- Fundo recomendado: transparente.
- Estilo recomendado: rosto/cabeca centralizada, com boa leitura quando reduzida.
- Area segura: deixe a cabeca ocupar quase todo o canvas, mas evite cortar bordas importantes.
- Animacoes nao sao necessarias.

O HUD le a imagem inteira `32x32` e renderiza em tamanho menor, atualmente `18x18`. Voce nao precisa cortar a imagem para `18x18`; mantenha a face centralizada no canvas `32x32` e deixe o mod reduzir visualmente no Hub.

Como a imagem sera reduzida, detalhes muito pequenos podem desaparecer. Uma imagem `32x32` simples, contrastada e com o rosto ocupando boa parte do canvas costuma funcionar melhor.

## Pasta Correta

Coloque as imagens dentro de:

```text
src/main/resources/assets/dynamicportals/textures/gui/mob_heads/
```

A estrutura usa o namespace do mob como subpasta.

## Regra De Nome

O nome do arquivo vem diretamente do id do mob usado na config.

Formato do id:

```text
namespace:path
```

Formato do arquivo:

```text
src/main/resources/assets/dynamicportals/textures/gui/mob_heads/<namespace>/<path>.png
```

## Exemplos Vanilla

Para o mob:

```text
minecraft:zombie
```

Crie:

```text
src/main/resources/assets/dynamicportals/textures/gui/mob_heads/minecraft/zombie.png
```

Para o mob:

```text
minecraft:wither_skeleton
```

Crie:

```text
src/main/resources/assets/dynamicportals/textures/gui/mob_heads/minecraft/wither_skeleton.png
```

Para o mob:

```text
minecraft:piglin_brute
```

Crie:

```text
src/main/resources/assets/dynamicportals/textures/gui/mob_heads/minecraft/piglin_brute.png
```

## Exemplos Com Mods

Para um mob modded simples:

```text
twilightforest:naga
```

Crie:

```text
src/main/resources/assets/dynamicportals/textures/gui/mob_heads/twilightforest/naga.png
```

Para um mob com caminho composto:

```text
examplemod:bosses/fire_golem
```

Crie:

```text
src/main/resources/assets/dynamicportals/textures/gui/mob_heads/examplemod/bosses/fire_golem.png
```

Ou seja: se o path do id tem barras, mantenha as barras como subpastas.

## Como Descobrir O ID Do Mob

Use exatamente o mesmo id configurado em `dynamicportals-common.toml`.

Exemplo de requisito:

```toml
"minecraft:the_nether|minecraft:zombie|1"
```

O id do mob e:

```text
minecraft:zombie
```

Logo, a imagem deve ser:

```text
mob_heads/minecraft/zombie.png
```

## Como Testar

1. Crie a imagem PNG no caminho correto.
2. Inicie o client ou reinicie o jogo se ele ja estava aberto.
3. Abra o Hub com `H`.
4. Va em `Progress`.
5. Clique em `Details` no portal que possui o requisito de kill.
6. Confira se a cabeca aparece na linha do mob.

Se a imagem nao aparecer:

- confirme se o arquivo e `.png`;
- confirme se o namespace esta correto;
- confirme se o nome do arquivo bate com o path do mob;
- confirme se a imagem esta dentro de `assets/dynamicportals/textures/gui/mob_heads/`;
- reinicie o client para garantir que os assets foram recarregados.

## Observacoes

- Nao e necessario criar imagens para todos os mobs.
- Mobs sem imagem customizada continuam funcionando normalmente.
- O sistema nao altera requisitos, progresso ou config; ele muda apenas a apresentacao visual no Hub.
- Evite nomes com letras maiusculas. Resource locations do Minecraft usam nomes em minusculo.
