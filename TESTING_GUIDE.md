# Guia de Testes - Dynamic Portals v2.0

## 🧪 Testes Essenciais

### 1. Teste de Remoção do Sistema de Reset por Mortes

**Objetivo:** Verificar que mortes não resetam mais o progresso

**Passos:**
1. Inicie o jogo em modo sobrevivência
2. Mate alguns mobs (Zombie, Skeleton, Creeper)
3. Abra o HUD (tecla P)
4. Verifique que os mobs aparecem como matados (☑)
5. **Morra propositalmente 15+ vezes**
6. Abra o HUD novamente
7. **Resultado Esperado:** ✅ Progresso de kills mantido, sem reset
8. **Resultado Esperado:** ✅ Contador de mortes NÃO aparece no HUD

---

### 2. Teste do Sistema de Tags

**Objetivo:** Verificar que tags estão carregando mobs corretamente

**Passos:**
1. Execute o comando: `/tag @s add test`
2. Verifique os logs do servidor para:
   ```
   [Dynamic Portals] Common setup phase...
   [Dynamic Portals] Loading mod compatibility configurations...
   ```
3. Use `/data get entity @e[type=minecraft:zombie,limit=1]` para verificar tags
4. **Resultado Esperado:** ✅ Mobs vanilla têm tags `dynamicportals:overworld_progression`

---

### 3. Teste de Compatibilidade - Mowzie's Mobs

**Pré-requisito:** Instalar Mowzie's Mobs mod

**Passos:**
1. Inicie o servidor/cliente com Mowzie's Mobs instalado
2. Verifique nos logs:
   ```
   [Dynamic Portals] Loaded compatibility config for mod: mowziesmobs (X mobs, Y bosses)
   ```
3. Spawne um Foliaath: `/summon mowziesmobs:foliaath`
4. Mate o Foliaath
5. Abra o HUD (tecla P)
6. **Resultado Esperado:** ✅ Foliaath aparece na lista de overworld mobs
7. **Resultado Esperado:** ✅ Kill é contabilizado para progresso

**Testes Adicionais:**
- Spawne e mate Ferrous Wroughtnaut (boss)
- Verifique que aparece na seção de bosses
- Tente entrar no Nether após completar todos os requisitos

---

### 4. Teste de Compatibilidade - Mod Não Instalado

**Objetivo:** Verificar que configs de mods não instalados são ignoradas

**Passos:**
1. Remova/não instale Alex's Mobs
2. Inicie o servidor
3. Verifique logs: NÃO deve mostrar "Loaded compatibility config for mod: alexsmobs"
4. **Resultado Esperado:** ✅ Nenhum erro ou warning
5. **Resultado Esperado:** ✅ Mod inicia normalmente

---

### 5. Teste de Sincronização Cliente-Servidor

**Pré-requisito:** Servidor dedicado + cliente

**Passos:**
1. Conecte ao servidor
2. Mate alguns mobs
3. Abra o HUD (tecla P)
4. **Resultado Esperado:** ✅ Progresso aparece corretamente
5. Desconecte e reconecte
6. Abra o HUD novamente
7. **Resultado Esperado:** ✅ Progresso foi persistido (salvo no NBT)

---

### 6. Teste de Bloqueio de Portal

**Objetivo:** Verificar que portais continuam bloqueados corretamente

**Passos:**
1. Crie um portal do Nether
2. **SEM** completar os requisitos, tente entrar
3. **Resultado Esperado:** ✅ Entrada bloqueada
4. **Resultado Esperado:** ✅ Mensagem: "message.dynamicportals.portal_blocked.nether"
5. Complete todos os requisitos (mate todos os mobs + pegue diamante)
6. Tente entrar no portal novamente
7. **Resultado Esperado:** ✅ Teleporte funciona normalmente

---

### 7. Teste de Advancement Trigger

**Objetivo:** Verificar que achievements desbloqueiam corretamente

**Passos:**
1. Complete todos os requisitos do Nether:
   - Mate todos os mobs de `overworld_progression` tag
   - Mate Elder Guardian
   - Pegue um Diamante
2. **Resultado Esperado:** ✅ Advancement "Nether Access" é desbloqueado
3. Verifique com: `/advancement grant @s only dynamicportals:nether_access`
4. **Resultado Esperado:** ✅ Portal do Nether funciona

---

### 8. Teste de HUD - Alternância de Fases

**Objetivo:** Verificar que Tab alterna entre overworld/nether phases

**Passos:**
1. Abra o HUD (tecla P)
2. Pressione Tab
3. **Resultado Esperado:** ✅ HUD muda para mostrar requisitos do Nether → End
4. Pressione Tab novamente
5. **Resultado Esperado:** ✅ HUD volta para Overworld → Nether

---

### 9. Teste de Datapack Customizado

**Objetivo:** Verificar que datapacks podem adicionar mobs às tags

**Passos:**
1. Crie um datapack:
   ```
   custom_pack/
     pack.mcmeta
     data/
       dynamicportals/
         tags/
           entity_types/
             overworld_progression.json
   ```

2. Adicione ao JSON:
   ```json
   {
     "replace": false,
     "values": [
       "minecraft:zombie_villager"
     ]
   }
   ```

3. Carregue o datapack: `/reload`
4. Mate um Zombie Villager
5. **Resultado Esperado:** ✅ Kill é contabilizado para progresso

---

### 10. Teste de Performance

**Objetivo:** Verificar que o sistema não causa lag

**Passos:**
1. Spawne 100 mobs: `/fill ~ ~ ~ ~10 ~ ~10 minecraft:air replace`
2. Use `/summon minecraft:zombie ~ ~ ~ {Tags:["test"]}` 100x
3. Mate todos os mobs rapidamente
4. **Resultado Esperado:** ✅ Sem lag perceptível
5. **Resultado Esperado:** ✅ Todos os kills são contabilizados

---

## 🐛 Checklist de Debugging

Se algo não funcionar, verifique:

- [ ] Logs mostram "Dynamic Portals mod initialized successfully!"
- [ ] Logs mostram "Loaded X mod compatibility configuration(s)"
- [ ] Arquivo `options.txt` tem a keybind configurada
- [ ] Servidor está sincronizando pacotes (veja network traffic)
- [ ] NBT data está sendo salvo (veja `playerdata` pasta)
- [ ] Tags estão no lugar certo (`data/dynamicportals/tags/entity_types/`)
- [ ] JSONs de compat estão válidos (use validator JSON online)

---

## 📊 Comandos Úteis para Debug

```bash
# Ver progresso de um jogador
/data get entity @p

# Forçar grant de advancement
/advancement grant @s only dynamicportals:nether_access

# Verificar tags de uma entidade
/data get entity @e[type=zombie,limit=1] Tags

# Recarregar datapacks
/reload

# Ver lista de advancements
/advancement list

# Spawnar boss de Mowzie's Mobs
/summon mowziesmobs:ferrous_wroughtnaut ~ ~ ~
```

---

## ✅ Resultado Final Esperado

Após todos os testes:

1. ✅ Sistema de reset por mortes completamente removido
2. ✅ Tags funcionando corretamente
3. ✅ Mods compatíveis detectados automaticamente
4. ✅ Progresso sincronizado entre cliente-servidor
5. ✅ Portais bloqueando/desbloqueando corretamente
6. ✅ HUD mostrando progresso atualizado
7. ✅ Datapacks podem estender o sistema
8. ✅ Performance mantida (sem lag)

---

**Data dos Testes:** _____________

**Testador:** _____________

**Versão do Mod:** 2.0

**Versão do Minecraft:** 1.21.1

**NeoForge:** Latest
