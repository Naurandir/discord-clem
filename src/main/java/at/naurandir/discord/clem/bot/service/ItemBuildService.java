package at.naurandir.discord.clem.bot.service;

import at.naurandir.discord.clem.bot.model.item.ItemBuild;
import at.naurandir.discord.clem.bot.model.item.ItemBuildMapper;
import at.naurandir.discord.clem.bot.model.item.Warframe;
import at.naurandir.discord.clem.bot.model.item.Weapon;
import at.naurandir.discord.clem.bot.repository.ItemBuildRepository;
import at.naurandir.discord.clem.bot.service.client.WarframeClient;
import at.naurandir.discord.clem.bot.service.client.dto.overframe.OverframeBuildDTO;
import at.naurandir.discord.clem.bot.service.client.dto.overframe.OverframeDTO;
import at.naurandir.discord.clem.bot.service.client.dto.overframe.OverframeItemDTO;
import at.naurandir.discord.clem.bot.service.client.dto.overframe.OverframeItemTierDTO;
import at.naurandir.discord.clem.bot.service.client.dto.overframe.OverframeItemTierListDTO;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 *
 * @author Naurandir
 */
@Slf4j
@Service
public class ItemBuildService extends SyncService {

    @Autowired
    private ItemBuildMapper itemBuildMapper;

    @Autowired
    private ItemBuildRepository itemBuildRepository;

    @Autowired
    private WarframeService warframeService;

    @Autowired
    private WeaponService weaponService;

    @Autowired
    private WarframeClient warframeClient;

    @Value("${discord.clem.build.base.url}")
    private String apiBaseUrl;
    
    @Value("${discord.clem.build.media.url}")
    private String apiMediaUrl;
    
    @Value("${discord.clem.build.tier.url}")
    private String apiTierListUrl;

    @Value("${discord.clem.build.item.url}")
    private String apiItemUrl;

    @Value("#{${discord.clem.build.headers}}")
    private Map<String, String> apiHeaders;
    
    @Override
    @Transactional
    @Scheduled(cron = "${discord.clem.build.scheduler.cron}")
    public void sync() throws IOException {
        try {
            OverframeDTO overframe = getCurrentBuilds();
        
        syncWarframeBuilds(overframe);
        syncWeaponBuilds(overframe);
        } catch (Exception ex) {
            log.error("sync throwed exception: ", ex);
        }
    }

    private void syncWeaponBuilds(OverframeDTO overframe) {
        List<Weapon> weaponsDb = weaponService.getWeapons();
        for (Weapon weapon : weaponsDb) {
            OverframeItemDTO overframeItem = overframe.getWeapons().stream()
                    .filter(weaponOverframe -> weaponOverframe.getName().equals(weapon.getName()))
                    .findFirst().orElse(null);
            if (overframeItem == null) {
                log.warn("syncBuilds: no overframe weapon found for weapon {}", weapon.getName());
                continue;
            }
            
            // update thumbnail if missing
            if (!isEmpty(overframeItem.getPictureUrl()) && isEmpty(weapon.getWikiaThumbnail())) {
                weaponService.updateWikiThumbnail(weapon, overframeItem.getPictureUrl());
            }
            
            // remove builds
            Set<ItemBuild> oldBuilds = itemBuildRepository.findByWeaponAndEndDateIsNull(weapon);
            if (!oldBuilds.isEmpty()) {
                itemBuildRepository.deleteAll(oldBuilds);
            }
            
            // add current builds
            Set<ItemBuild> newBuilds = new HashSet<>();
            for (OverframeBuildDTO overframeBuild : overframeItem.getBuilds()) {
                ItemBuild newbuild = itemBuildMapper.fromDtoToBuild(overframeBuild);
                newbuild.setWeapon(weapon);
                newBuilds.add(newbuild);
            }
            itemBuildRepository.saveAll(newBuilds);
        }
    }
    
    private void syncWarframeBuilds(OverframeDTO overframe) {
        List<Warframe> warframesDb = warframeService.getWarframes();
        for (Warframe warframe : warframesDb) {
            OverframeItemDTO overframeItem = overframe.getWarframes().stream()
                    .filter(warframeOverframe -> warframeOverframe.getName().equals(warframe.getName()))
                    .findFirst().orElse(null);
            if (overframeItem == null) {
                log.warn("syncBuilds: no overframe warframe found for warframe {}", warframe.getName());
                continue;
            }
            
            // update thumbnail if missing
            if (!isEmpty(overframeItem.getPictureUrl()) && isEmpty(warframe.getWikiaThumbnail())) {
                warframeService.updateWikiThumbnail(warframe, overframeItem.getPictureUrl());
            }
            
            // remove builds
            Set<ItemBuild> oldBuilds = itemBuildRepository.findByWarframeAndEndDateIsNull(warframe);
            if (!oldBuilds.isEmpty()) {
                itemBuildRepository.deleteAll(oldBuilds);
            }
            
            // add current builds
            Set<ItemBuild> newBuilds = new HashSet<>();
            for (OverframeBuildDTO overframeBuild : overframeItem.getBuilds()) {
                ItemBuild newBuild = itemBuildMapper.fromDtoToBuild(overframeBuild);
                newBuild.setWarframe(warframe);
                newBuilds.add(newBuild);
            }
            itemBuildRepository.saveAll(newBuilds);
        }
    }

    private OverframeDTO getCurrentBuilds() throws IOException {
        OverframeDTO dto = new OverframeDTO();

        OverframeItemTierListDTO warframeTierlist
                = warframeClient.getData(apiTierListUrl.replace("{type}", "0"),
                        apiHeaders,
                        OverframeItemTierListDTO.class);

        OverframeItemTierListDTO primaryWeaponTierlist
                = warframeClient.getData(apiTierListUrl.replace("{type}", "1"),
                        apiHeaders,
                        OverframeItemTierListDTO.class);

        OverframeItemTierListDTO secondaryTierlist
                = warframeClient.getData(apiTierListUrl.replace("{type}", "2"),
                        apiHeaders,
                        OverframeItemTierListDTO.class);

        OverframeItemTierListDTO meeleTierlist
                = warframeClient.getData(apiTierListUrl.replace("{type}", "3"),
                        apiHeaders,
                        OverframeItemTierListDTO.class);
        
        OverframeItemTierListDTO archTierlist
                = warframeClient.getData(apiTierListUrl.replace("{type}", "4"),
                        apiHeaders,
                        OverframeItemTierListDTO.class);

        List<OverframeItemDTO> warframes = new ArrayList<>();
        for (OverframeItemTierDTO itemTier : warframeTierlist.getTierList()) {
            OverframeItemDTO item = getOverFrameItem(String.valueOf(itemTier.getItemId()));
            if (item == null) {
                log.warn("getCurrentBuilds: no warframe found for item id [{}]", itemTier.getItemId());
                continue;
            }
            warframes.add(item);
        }

        List<OverframeItemDTO> weapons = new ArrayList<>();
        for (OverframeItemTierDTO itemTier : primaryWeaponTierlist.getTierList()) {
            OverframeItemDTO item = getOverFrameItem(String.valueOf(itemTier.getItemId()));
            if (item == null) {
                log.warn("getCurrentBuilds: no primary weapon found for item id [{}]", itemTier.getItemId());
                continue;
            }
            weapons.add(item);
        }

        for (OverframeItemTierDTO itemTier : secondaryTierlist.getTierList()) {
            OverframeItemDTO item = getOverFrameItem(String.valueOf(itemTier.getItemId()));
            if (item == null) {
                log.warn("getCurrentBuilds: no secondary weapon found for item id [{}]", itemTier.getItemId());
                continue;
            }
            weapons.add(item);
        }

        for (OverframeItemTierDTO itemTier : meeleTierlist.getTierList()) {
            OverframeItemDTO item = getOverFrameItem(String.valueOf(itemTier.getItemId()));
            if (item == null) {
                log.warn("getCurrentBuilds: no meele weapon found for item id [{}]", itemTier.getItemId());
                continue;
            }
            weapons.add(item);
        }
        
        for (OverframeItemTierDTO itemTier : archTierlist.getTierList()) {
            OverframeItemDTO item = getOverFrameItem(String.valueOf(itemTier.getItemId()));
            if (item == null) {
                log.warn("getCurrentBuilds: no arch weapon found for item id [{}]", itemTier.getItemId());
                continue;
            }
            weapons.add(item);
        }

        dto.setWarframes(warframes);
        dto.setWeapons(weapons);

        return dto;
    }
    
    private OverframeItemDTO getOverFrameItem(String id) throws IOException {
        OverframeItemDTO item = new OverframeItemDTO();

        String htmlString = warframeClient.getDataRaw(
                apiItemUrl.replace("{id}", String.valueOf(id)),
                apiHeaders);
        if (isEmpty(htmlString)) {
            return null;
        }
        
        Document html = Jsoup.parse(htmlString);

        // title
        // Title: Warframe Kuva Zarr - Warframe Kuva Zarr Builds - Overframe
        item.setName(html.select("title").first().text().split(" - ")[0].replace("Warframe ", ""));

        // picture url
        Elements metaElements = html.select("meta");
        for (Element metaElement : metaElements) {
            String content = metaElement.attr("content");
            if (content.startsWith(apiMediaUrl) && content.contains("Icons")) {
                item.setPictureUrl(content);
                break;
            }
        }

        // Builds
        item.setBuilds(new ArrayList<>());
        Elements buildElements = html.select("article").get(1).getElementsByTag("a");
        for (Element build : buildElements) {
            OverframeBuildDTO buildDTO = new OverframeBuildDTO();

            buildDTO.setUrl(apiBaseUrl + build.attr("href"));
            buildDTO.setVotes(Integer.parseInt(build.child(2).text().replace("Votes ", "")));

            Element buildData = build.child(1);

            buildDTO.setTitle(buildData.child(0).text());
            buildDTO.setFormasUsed(buildData.child(2).child(0).text());

            if (buildData.child(2).childrenSize() > 1) {
                buildDTO.setGuideLength(buildData.child(2).child(1).text());
            } else {
                buildDTO.setGuideLength("Normal Guide");
            }

            item.getBuilds().add(buildDTO);
        }

        return item;
    }

    @Override
    boolean isFirstTimeStartup() {
        return itemBuildRepository.count() == 0L;
    }
}
