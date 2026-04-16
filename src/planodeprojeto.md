O Objetivo desse Mod é criar restrições para o acesso dos portais do jogo de forma progressivamente lógica.

1. Em primeiro lugar tudo deve ser de fácil configuração através de um arquivo .toml que fique na pasta config do jogo.

2. Irão existir os seguintes tipos de restrição:
1 - (Padrão) Matar mobs/bosses específicos e em quantidades específicas. Tanto os mobs/bosses como a quantidade deles que devem ser mortos deve ser escolhido/editado através do arquivo de configuração.
2 - (Opcional) Adiquirir itens específicos do jogo. Tanto quais como quantos devem ser escolhidos/editados através do arquivo de configuração. Vem sem nenhum por padrão por enquanto.
3 - (Opcional) Atingir conquistas do jogo. Quais devem ser escolhidas/editadas através do arquivo de configuração. Vem sem nenhuma por padrão por enquanto.

3. Agora players deverão poder consumir o item Creme de Magma que dropa dos Magma Cubes e os players que fizerem isso terão seu acesso ao Portal do Nether liberado. Qualquer player após consumir uma Fruta do Coro também deve ter seu acesso ao portal do The End liberado. Isso servirá para facilitar que players que jogam bastante, ajudem players que tem menos tempo pra jogar o jogo e cumprir os requisitos em sua totalidade. E claro, como tudo que estamos conversando, isso também deve ser configurável/editável.

4. O que deve vir por padrão configurado já no Arquivo:
- Para liberar o Portal do Nether - 
* Mob Kill List:
Zombie x1
Skeleton x1
Spider x1
Creeper x1
Slime x1
Witch x1
Husks x1
Pillager x1
Vindicator x1
Bogged x1
Breeze x1
Evoker x1
Ravager x1
Elder Guardian x1
* Item List:
Deixe somente a estrutura com um exemplo mas que não funcione, somente ilustrativo, os players ditarão aqui caso queiram algo.
* Achievements List:
Deixe somente a estrutura com um exemplo mas que não funcione, somente ilustrativo, os players ditarão aqui caso queiram algo.

- Para liberar o Portal do The End -
* Mob Kill List:
Magma Cube x1
Blaze x1 
Wither Skeleton x1
Piglin x1
Hoglin x1
Piglin Brute x1
Ghast x1
The Wither x1
Warden x1
* Item List:
Deixe somente a estrutura com um exemplo mas que não funcione, somente ilustrativo, os players editarão eles mesmos caso queiram algo aqui.
* Achievements List:
Deixe somente a estrutura com um exemplo mas que não funcione, somente ilustrativo, os players editarão eles mesmos caso queiram algo aqui.

5. Para que o Player consiga acompanhar o seu progresso em tempo real no jogo devemos ter o seguinte sistema de chat:
- Comando /check: Mostra a lista completa.
- Notificações Automáticas: Toda vez que o player matar um mob, encontrar um item ou alcançar uma conquista que está configurada no .toml, o mod envia uma mensagem no chat do jogo, algo como:
Player: Steve - "Progresso: [Zumbi 8/10] - Falta pouco!"
- Som de Sucesso: Quando ele completar um requisito (ex: terminou os 10 zumbis), toca um som de "Level Up" ou de "Orbe de XP" apenas para ele e uma mensagem no chat do jogo avisando que ele completou aquela tarefa.
- Quando um player cumprir todos os requisitos e liberar algum dos portais, também deve ser comunicado no chat.
Deixo pra que você expanda como queira esse sistema de comunicação via chat pra que ele seja lindo e completo.

6. Algumas explicações a mais:
- O Mod deve ser completamente configurável pelo arquivo .toml justamente para ter compatibilidade com mobs de mods caso o player queira colocar eles como requisito. O Player apenas vai lá e escreve e pronto, simples e prático. A mesma coisa com portais adicionados por mods, o player simplesmente deve poder adicionar o portal e escrever sua lista de restrição. Assim os players poderão adaptar esse mod aos seus modpacks de forma fácil e sem muita complicação.

7. Traga mais sugestões e ideias que melhorem o projeto mas mantendo essa base que expliquei nos itens de 1-6 como regra.

8. Adicione uma espada de madeira que mate qualquer mob do jogo com apenas 1 hit e que só pode ser dada através de um comando /give para o jogador, vou usar isso pra fase de testes do mod, pra não ter que matar tudo em "Modo Vannila" e adiantar o meu trabalho de testagem.

Vamos planejar, quero que crie um mod de Minecraft pra mim, serei o idealizador e testador do projeto, você será meu agente que escrevá todo o código do mod. Utilizaremos a API NeoForge, ModDevGradle, Minecraft versão 1.21.1. Toda a documentação NeoForge para essa versão em específico você pode encontrar na pasta neoforge no respositório do projeto pras suas consultas e pra te ajudar nesse projeto até o fim.