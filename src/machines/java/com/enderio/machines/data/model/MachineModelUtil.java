package com.enderio.machines.data.model;

import com.enderio.EnderIO;
import com.enderio.core.data.model.EIOModel;
import com.enderio.machines.common.block.ProgressMachineBlock;
import com.enderio.machines.common.block.SolarPanelBlock;
import com.enderio.machines.common.blockentity.solar.SolarPanelTier;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import com.tterrag.registrate.providers.RegistrateItemModelProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.client.model.generators.BlockModelBuilder;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.client.model.generators.loaders.CompositeModelBuilder;

import java.util.Locale;

public class MachineModelUtil {

    // region Block states

    public static void machineBlock(DataGenContext<Block, ? extends Block> ctx, RegistrateBlockstateProvider prov) {
        machineBlock(ctx, prov, MachineModelUtil::machineBody);
    }

    public static void soulMachineBlock(DataGenContext<Block, ? extends Block> ctx, RegistrateBlockstateProvider prov) {
        machineBlock(ctx, prov, MachineModelUtil::soulMachineBody);
    }

    public static void customMachineBlock(DataGenContext<Block, ? extends Block> ctx, RegistrateBlockstateProvider prov, String type) {
        machineBlock(ctx, prov, (_prov, name, frontModel) -> MachineModelUtil.customMachineBody(_prov, name, frontModel, type));
    }

    private static void machineBlock(DataGenContext<Block, ? extends Block> ctx, RegistrateBlockstateProvider prov, MachineBodyBuilder bodyBuilder) {
        // Create unpowered and powered bodies.
        String ns = ctx.getId().getNamespace();
        String path = ctx.getId().getPath();
        ModelFile unpowered = bodyBuilder.build(prov, ctx.getName(), EIOModel.getExistingParent(prov.models(), new ResourceLocation(ns, "block/" + path + "_front")));
        ModelFile powered = bodyBuilder.build(prov, ctx.getName() + "_on", prov
            .models()
            .withExistingParent(ctx.getName() + "_front_on", new ResourceLocation(ns, "block/" + path + "_front"))
            .texture("front", EnderIO.loc("block/" + ctx.getName() + "_front_on")));
        machineBlock(ctx, prov, unpowered, powered);
    }
    public static void solarPanel(DataGenContext<Block, ? extends Block> ctx, RegistrateBlockstateProvider prov, SolarPanelTier tier) {
        String tierName = tier.name().toLowerCase(Locale.ROOT);
        var baseModel = prov.models().withExistingParent(ctx.getName() + "_base", EnderIO.loc("block/photovoltaic_cell_base")).texture("panel", "block/" + tierName + "_top").texture("side", "block/" + tierName + "_side");
        var sideModel = prov.models().withExistingParent(ctx.getName() + "_side", EnderIO.loc("block/photovoltaic_cell_side")).texture("side", "block/" + tierName + "_side");
        var cornerModel = prov.models().withExistingParent(ctx.getName() + "_corner", EnderIO.loc("block/photovoltaic_cell_corner")).texture("side", "block/" + tierName + "_side");
        var builder = prov.getMultipartBuilder(ctx.get());
        builder.part().modelFile(baseModel).addModel();
        builder.part().modelFile(sideModel).addModel().condition(SolarPanelBlock.NORTH, true);
        builder.part().modelFile(sideModel).rotationY(90).addModel().condition(SolarPanelBlock.EAST, true);
        builder.part().modelFile(sideModel).rotationY(180).addModel().condition(SolarPanelBlock.SOUTH, true);
        builder.part().modelFile(sideModel).rotationY(270).addModel().condition(SolarPanelBlock.WEST, true);
        builder.part().modelFile(cornerModel).addModel().condition(SolarPanelBlock.NORTH_EAST, true);
        builder.part().modelFile(cornerModel).rotationY(90).addModel().condition(SolarPanelBlock.SOUTH_EAST, true);
        builder.part().modelFile(cornerModel).rotationY(180).addModel().condition(SolarPanelBlock.SOUTH_WEST, true);
        builder.part().modelFile(cornerModel).rotationY(270).addModel().condition(SolarPanelBlock.NORTH_WEST, true);
    }

    public static void solarPanel(DataGenContext<Item, ? extends Item> ctx, RegistrateItemModelProvider prov, SolarPanelTier tier) {
        String tierName = tier.name().toLowerCase(Locale.ROOT);
        prov.withExistingParent(ctx.getName(), EnderIO.loc("item/photovoltaic_cell")).texture("side", "block/" + tierName + "_side").texture("panel", "block/" + tierName + "_top");
    }

    private static void machineBlock(DataGenContext<Block, ? extends Block> ctx, RegistrateBlockstateProvider prov, ModelFile unpowered, ModelFile powered) {
        prov
            .getVariantBuilder(ctx.get())
            .forAllStates(state -> ConfiguredModel
                .builder()
                .modelFile(state.getValue(ProgressMachineBlock.POWERED) ? powered : unpowered)
                .rotationY(((int) state.getValue(BlockStateProperties.HORIZONTAL_FACING).toYRot() + 180) % 360)
                .build());
    }

    // endregion

    // region Models

    @FunctionalInterface
    private interface MachineBodyBuilder {
        ModelFile build(RegistrateBlockstateProvider prov, String name, BlockModelBuilder frontModel);
    }

    private static ModelFile machineBody(RegistrateBlockstateProvider prov, String name, BlockModelBuilder frontModel) {
        return machineBodyModel(prov, name, frontModel, "machine");
    }

    private static ModelFile soulMachineBody(RegistrateBlockstateProvider prov, String name, BlockModelBuilder frontModel) {
        return machineBodyModel(prov, name, frontModel, "soul_machine");
    }

    private static ModelFile customMachineBody(RegistrateBlockstateProvider prov, String name, BlockModelBuilder frontModel, String type) {
        return machineBodyModel(prov, name, frontModel, type);
    }

    private static ModelFile machineBodyModel(RegistrateBlockstateProvider prov, String name, BlockModelBuilder frontModel, String type) {
        return prov.models().withExistingParent(name, prov.mcLoc("block/block"))
            .customLoader(CompositeModelBuilder::begin)
                .child("frame", EIOModel.getExistingParent(prov.models(), EnderIO.loc("block/" + type + "_frame")))
                .child("overlay", EIOModel.getExistingParent(prov.models(), EnderIO.loc("block/io_overlay")))
                .child("front", frontModel)
            .end()
            .texture("particle", EnderIO.loc("block/" + type + "_side"));
    }

    // endregion
}
