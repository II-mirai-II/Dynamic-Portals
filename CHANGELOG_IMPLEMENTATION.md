# Changelog - Sistema de Compatibilidade e Remoção de Reset por Mortes

## Data: 4 de dezembro de 2025

### 🗑️ Removido: Sistema de Reset por Mortes

**Motivação:** A mecânica de resetar o progresso do jogador a cada 10 mortes era frustrante e punitiva demais.

#### Arquivos Modificados:

1. **PlayerProgressData.java**
   - ❌ Removido campo `deathCount`
   - ❌ Removidos métodos: `incrementDeathCount()`, `getDeathCount()`, `resetDeathCount()`, `shouldResetProgress()`, `resetProgress()`, `isNetherMob()`
   - ✅ Simplificado o sistema de persistência NBT

2. **PlayerEventHandler.java**
   - ❌ Removido completamente o método `onPlayerDeath()`
   - ✅ Mantidos: `onPlayerClone()`, `onItemPickup()`, `onPlayerLoggedIn()`

3. **SyncProgressPacket.java**
   - ❌ Removido campo `deathCount` do record
   - ✅ Atualizado STREAM_CODEC
   - ✅ Atualizado método `fromProgressData()`

4. **ClientProgressCache.java**
   - ❌ Removido campo estático `deathCount`
   - ❌ Removido método `getDeathCount()`

5. **ProgressHUD.java**
   - ❌ Removida renderização do contador de mortes
   - ❌ Removido método `getDeathCountColor()`
   - ✅ Ajustado cálculo de altura do HUD

6. **ModConstants.java**
   - ❌ Removidas constantes: `DEATH_THRESHOLD`, `MSG_PROGRESS_RESET`, `HUD_DEATHS`, `NBT_DEATH_COUNT`

**Impacto:** Jogadores agora podem morrer quantas vezes quiserem sem perder progresso. O progresso de kills e itens é permanente até que um achievement seja desbloqueado.

---

### ✨ Novo: Sistema de Compatibilidade com Mods

**Motivação:** Permitir que o mod funcione dinamicamente com outros mods que adicionam criaturas, sem necessidade de recompilar código.

#### Nova Arquitetura:

##### 1. Sistema de Tags de Entidades

Criadas 6 tags para categorização de mobs:

```
data/dynamicportals/tags/entity_types/
├─ overworld_progression.json  (15 mobs vanilla)
├─ nether_progression.json     (6 mobs vanilla)
├─ end_progression.json        (2 mobs vanilla)
├─ bosses_overworld.json       (Elder Guardian)
├─ bosses_nether.json          (Warden, Wither)
└─ bosses_end.json             (Ender Dragon)
```

**Vantagem:** Datapacks podem adicionar mobs às tags sem modificar código.

##### 2. Sistema de Configuração JSON

Nova estrutura para compatibilidade com mods:

```
data/dynamicportals/mod_compat/
├─ mowziesmobs.json
├─ alexsmobs.json
├─ twilightforest.json
└─ iceandfire.json
```

Formato do JSON:
```json
{
  "mod_id": "mowziesmobs",
  "enabled": true,
  "overworld_mobs": ["mowziesmobs:foliaath", ...],
  "nether_mobs": ["mowziesmobs:barakoa", ...],
  "end_mobs": [],
  "bosses": ["mowziesmobs:ferrous_wroughtnaut", ...]
}
```

##### 3. Nova Classe: ModCompatibilityRegistry

**Localização:** `com.mirai.dynamicportals.compat.ModCompatibilityRegistry`

**Funcionalidades:**
- ✅ Detecta mods instalados via `ModList.get().isLoaded()`
- ✅ Carrega automaticamente JSONs de compatibilidade
- ✅ Resolve entity IDs para EntityType
- ✅ Fornece API para obter mobs por categoria
- ✅ Logging detalhado de carregamento

**API Pública:**
```java
ModCompatibilityRegistry.isModLoaded(String modId)
ModCompatibilityRegistry.getAllOverworldMobs()
ModCompatibilityRegistry.getAllNetherMobs()
ModCompatibilityRegistry.getAllBosses()
ModCompatibilityRegistry.getConfig(String modId)
```

##### 4. Atualização: PortalRequirement.Builder

**Novos Métodos:**

```java
// Adicionar mobs de uma tag
.addMobsFromTag(TagKey<EntityType<?>> tag)
.addBossesFromTag(TagKey<EntityType<?>> tag)

// Adicionar mobs de uma lista
.addMobsList(List<EntityType<?>> mobs)
.addBossesList(List<EntityType<?>> bosses)
```

**Uso:**
```java
PortalRequirement.builder(dimension)
    .addMobsFromTag(overworldTag)  // Carrega mobs da tag
    .addMobsList(compatMobs)       // Adiciona mobs de outros mods
    .build();
```

##### 5. Integração no DynamicPortals

**Fluxo de Inicialização:**

```java
commonSetup() {
    1. ModCompatibilityRegistry.loadCompatibilityConfigs()
       ↓ Detecta mods instalados
       ↓ Carrega JSONs de compatibilidade
    
    2. PortalRequirementRegistry.registerVanillaRequirements()
       ↓ Cria requisitos usando tags
       ↓ Adiciona mobs de compatibilidade automaticamente
}
```

#### Mods com Compatibilidade Incluída:

1. **Mowzie's Mobs**
   - 4 overworld mobs, 2 nether mobs, 3 bosses

2. **Alex's Mobs**
   - 7 overworld mobs, 3 nether mobs, 2 end mobs, 2 bosses

3. **Twilight Forest**
   - 8 overworld mobs, 9 bosses

4. **Ice and Fire**
   - 12 overworld mobs, 6 bosses (dragões!)

---

### 📚 Documentação Criada

1. **ARCHITECTURE.md** - Diagrama completo de dependências e arquitetura
2. **MOD_COMPATIBILITY.md** - Guia completo do sistema de compatibilidade

---

### 🔧 Como Usar (Para Usuários)

1. **Instale Dynamic Portals + qualquer mod suportado**
2. **O sistema detecta automaticamente**
3. **Novos mobs aparecem nos requisitos**
4. **Sem configuração necessária!**

### 🛠️ Como Adicionar Suporte a Novo Mod (Para Desenvolvedores)

**Opção 1: Via Datapack**
```
Create: data/dynamicportals/mod_compat/yourmod.json
```

**Opção 2: Via Tag**
```
Add entities to: dynamicportals:overworld_progression
```

**Opção 3: Via API Programática**
```java
DynamicPortals.getAPI().registerPortalRequirement(...)
```

---

### ✅ Checklist de Implementação

- [x] Remover sistema de reset por mortes
- [x] Criar estrutura de tags de entidades
- [x] Implementar ModCompatibilityRegistry
- [x] Criar sistema de carregamento JSON
- [x] Atualizar PortalRequirement.Builder
- [x] Integrar no DynamicPortals
- [x] Criar configs de exemplo (4 mods)
- [x] Documentar sistema completo

---

### 🎯 Próximos Passos Sugeridos

1. **Testar em ambiente de desenvolvimento**
   - Verificar carregamento de tags
   - Testar com Mowzie's Mobs instalado
   - Validar sincronização cliente-servidor

2. **Adicionar mais compatibilidades**
   - Born in Chaos
   - Aquaculture
   - The Abyss

3. **Melhorar HUD**
   - Mostrar mobs de mods compatíveis
   - Indicador visual de mod de origem

4. **Sistema de recompensas**
   - Achievements especiais para matar bosses de mods
   - Estatísticas por mod

---

### 🐛 Notas de Debugging

- Logs mostram: `Loaded compatibility config for mod: <modid>`
- Erros de entity ID desconhecido aparecem como warnings
- Cache de progresso é sincronizado automaticamente
- Tags são carregadas no startup do servidor

---

**Status:** ✅ Implementação Completa
**Versão de Dados:** 1 (sem migração necessária)
**Compatibilidade:** NeoForge 1.21.1
