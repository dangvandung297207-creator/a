package com.truemetallurgy.registry;

import com.truemetallurgy.TrueMetallurgy;
import com.truemetallurgy.menu.AssemblyMenu;
import com.truemetallurgy.menu.ForgeHearthMenu;
import com.truemetallurgy.menu.ForgingMenu;
import com.truemetallurgy.menu.GrindingMenu;
import com.truemetallurgy.menu.QuenchingMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Container menus. All workstation menus receive the block position as extra data. */
public final class ModMenus {
    private ModMenus() {}

    public static final DeferredRegister<MenuType<?>> MENUS =
        DeferredRegister.create(Registries.MENU, TrueMetallurgy.MOD_ID);

    public static final DeferredHolder<MenuType<?>, MenuType<ForgeHearthMenu>> FORGE =
        MENUS.register("forge_hearth", () -> IMenuTypeExtension.create(ForgeHearthMenu::new));
    public static final DeferredHolder<MenuType<?>, MenuType<ForgingMenu>> FORGING =
        MENUS.register("forging", () -> IMenuTypeExtension.create(ForgingMenu::new));
    public static final DeferredHolder<MenuType<?>, MenuType<QuenchingMenu>> QUENCHING =
        MENUS.register("quenching", () -> IMenuTypeExtension.create(QuenchingMenu::new));
    public static final DeferredHolder<MenuType<?>, MenuType<GrindingMenu>> GRINDING =
        MENUS.register("grinding", () -> IMenuTypeExtension.create(GrindingMenu::new));
    public static final DeferredHolder<MenuType<?>, MenuType<AssemblyMenu>> ASSEMBLY =
        MENUS.register("assembly", () -> IMenuTypeExtension.create(AssemblyMenu::new));
}
