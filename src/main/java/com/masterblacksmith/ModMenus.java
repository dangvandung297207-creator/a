package com.masterblacksmith;

import com.masterblacksmith.menu.AnvilForgingMenu;
import com.masterblacksmith.menu.AssemblyMenu;
import com.masterblacksmith.menu.ForgeHearthMenu;
import com.masterblacksmith.menu.GrindingMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** Compact, medieval-styled workshop menus. */
public class ModMenus {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, MasterBlacksmith.MOD_ID);

    public static final RegistryObject<MenuType<ForgeHearthMenu>> FORGE_HEARTH =
            MENUS.register("forge_hearth", () -> IForgeMenuType.create(ForgeHearthMenu::new));
    public static final RegistryObject<MenuType<AnvilForgingMenu>> ANVIL_FORGING =
            MENUS.register("anvil_forging", () -> IForgeMenuType.create(AnvilForgingMenu::new));
    public static final RegistryObject<MenuType<AssemblyMenu>> ASSEMBLY =
            MENUS.register("assembly", () -> IForgeMenuType.create(AssemblyMenu::new));
    public static final RegistryObject<MenuType<GrindingMenu>> GRINDING =
            MENUS.register("grinding", () -> IForgeMenuType.create(GrindingMenu::new));
}
