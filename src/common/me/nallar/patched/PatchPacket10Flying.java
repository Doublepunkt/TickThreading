package me.nallar.patched;

import javassist.is.faulty.Redirects;
import me.nallar.tickthreading.minecraft.TickThreading;
import me.nallar.tickthreading.util.TableFormatter;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetServerHandler;
import net.minecraft.network.packet.NetHandler;
import net.minecraft.network.packet.Packet10Flying;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.ServerConfigurationManager;

public abstract class PatchPacket10Flying extends Packet10Flying {
	@Override
	public boolean canProcessAsync() {
		return true;
	}

	@Override
	public void processPacket(NetHandler par1NetHandler) {
		NetServerHandler nsh = (NetServerHandler) par1NetHandler;
		EntityPlayerMP entityPlayerMP = nsh.playerEntity;
		if (nsh.teleported > 0) {
			nsh.lastPZ = this.zPosition;
			nsh.lastPX = this.xPosition;
			nsh.averageSpeed = -50d;
			nsh.teleported--;
		} else {
			if (TickThreading.instance.antiCheatNotify && moving && yPosition != -999.0D && stance != -999.0D) {
				long currentTime = System.currentTimeMillis();
				long time = Math.min(5000, currentTime - nsh.lastMovement);
				double dX = (xPosition - nsh.lastPX);
				double dZ = (zPosition - nsh.lastPZ);
				if (time == 0) {
					nsh.lastPZ += dZ;
					nsh.lastPX += dX;
				} else {
					nsh.lastMovement = currentTime;
					if (time < 1) {
						time = 1;
					}
					double speed = (Math.sqrt(dX * dX + dZ * dZ) * 1000) / time;
					//Log.info(speed + "\t" + dX + '\t' + dZ + '\t' + time + '\t' + moving + '\t' + yPosition + '\t' + stance);
					if (Double.isInfinite(speed) || Double.isNaN(speed)) {
						speed = 1;
					}
					double averageSpeed = (nsh.averageSpeed = ((nsh.averageSpeed * 10 + speed) / 11));
					ServerConfigurationManager serverConfigurationManager = MinecraftServer.getServer().getConfigurationManager();
					speed /= allowedSpeedMultiplier(entityPlayerMP);
					if ((currentTime + 30000) < nsh.lastNotify && !serverConfigurationManager.areCommandsAllowed(entityPlayerMP.username) && (averageSpeed > 50 || (!entityPlayerMP.isRiding() && averageSpeed > 20))) {
						if (TickThreading.instance.antiCheatKick) {
							nsh.kickPlayerFromServer("You moved too quickly. " + TableFormatter.formatDoubleWithPrecision(averageSpeed, 3) + "m/s");
						} else {
							entityPlayerMP.sendChatToPlayer("You moved too quickly. " + TableFormatter.formatDoubleWithPrecision(averageSpeed, 3) + "m/s");
						}
						Redirects.notifyAdmins(entityPlayerMP.username + " was travelling too fast: " + TableFormatter.formatDoubleWithPrecision(averageSpeed, 3) + "m/s");
						nsh.lastNotify = currentTime;
					}
					nsh.lastPZ = this.zPosition;
					nsh.lastPX = this.xPosition;
					synchronized (entityPlayerMP.loadedChunks) {
						par1NetHandler.handleFlying(this);
					}
				}
			} else {
				synchronized (entityPlayerMP.loadedChunks) {
					par1NetHandler.handleFlying(this);
				}
			}
		}
	}

	private static double allowedSpeedMultiplier(EntityPlayerMP entityPlayerMP) {
		for (int i = 0; i < 4; i++) {
			if (entityPlayerMP.inventory.armorItemInSlot(i) != null) {
				return 1.5;
			}
		}
		return 1;
	}
}
