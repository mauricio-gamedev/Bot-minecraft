import { world, system } from "@minecraft/server";
import { ActionFormData } from "@minecraft/server-ui";

const COMPANION_ID = "mccompanion:buddy";
const OWNER_PROPERTY = "mccompanion:owner";

const orders = [
  {
    event: "mccompanion:order_follow",
    title: "Seguir você",
    description: "Ela volta a acompanhar seus passos e recolher itens próximos."
  },
  {
    event: "mccompanion:order_explore",
    title: "Explorar",
    description: "Ela passeia por conta própria e recolhe itens que encontrar."
  },
  {
    event: "mccompanion:order_guard",
    title: "Proteger esta área",
    description: "Ela guarda este ponto e ataca monstros que entrarem no alcance."
  },
  {
    event: "mccompanion:order_stay",
    title: "Ficar aqui",
    description: "Ela interrompe o acompanhamento e aguarda neste local."
  },
  {
    event: "mccompanion:order_collect",
    title: "Coletar itens",
    description: "Ela explora por perto, recolhe itens derrubados e os entrega a você."
  }
];

const entityTamedSignal = world.afterEvents.entityTamed;
if (entityTamedSignal) {
  entityTamedSignal.subscribe((event) => {
    const entity = event.entity;
    const owner = event.tamingEntity;
    if (entity.typeId !== COMPANION_ID || owner.typeId !== "minecraft:player") return;

    try {
      entity.setDynamicProperty(OWNER_PROPERTY, owner.id);
    } catch (error) {
      console.warn("[MC Companion] Não foi possível registrar o dono: " + error);
    }
  });
}

world.afterEvents.playerInteractWithEntity.subscribe((event) => {
  const entity = event.target;
  const player = event.player;
  if (entity.typeId !== COMPANION_ID) return;

  // Food and equipment interactions are left to Minecraft; use an empty hand for the order menu.
  if (event.beforeItemStack || event.itemStack) return;

  system.runTimeout(() => {
    try {
      const storedOwner = entity.getDynamicProperty(OWNER_PROPERTY);
      const tameComponent = entity.getComponent("minecraft:tameable");
      const componentOwner = tameComponent?.tamedToPlayerId;
      const ownerId = typeof storedOwner === "string" ? storedOwner : componentOwner;

      if (ownerId !== player.id) return;
      showOrders(player, entity);
    } catch (error) {
      console.warn("[MC Companion] Menu indisponível: " + error);
    }
  }, 1);
});

async function showOrders(player, entity) {
  if (!entity.isValid) return;

  const form = new ActionFormData()
    .title("MC Companion")
    .body("Escolha uma ordem para sua companheira.")
    .button("Seguir você")
    .button("Explorar por perto")
    .button("Proteger esta área")
    .button("Ficar aqui")
    .button("Coletar itens");

  try {
    const response = await form.show(player);
    if (response.canceled || response.selection === undefined) return;
    if (!entity.isValid) return;

    const order = orders[response.selection];
    if (!order) return;

    entity.triggerEvent(order.event);
    player.sendMessage("§bMC Companion§r: " + order.description);
  } catch (error) {
    player.sendMessage("§cNão consegui abrir o menu agora. Tente novamente.");
    console.warn("[MC Companion] Erro no menu de ordens: " + error);
  }
}
