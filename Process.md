# Process.md — 작업 진행 관리 문서

> 이 문서는 Claude가 루프 방식으로 작업을 이어가기 위한 **단일 진실 공급원(SSOT)** 이다.
> CLAUDE.md의 원칙(구조 결정권은 사용자, 작은 작업 단위, 추측 금지)을 항상 우선한다.

---

## 0. 루프 프로토콜 (매 작업 사이클마다 수행)

1. **읽기**: `CLAUDE.md` → `Process.md`(이 문서) 순서로 읽는다.
2. **선택**: `2. 작업 큐`에서 위에서부터 첫 번째로 `[ ]`(대기) 상태이면서 `⛔ 결정필요`가 아닌 작업 1개를 고른다.
3. **구현**: 해당 작업만 수행한다. 작업 범위를 벗어난 리팩토링 금지.
4. **검증**: `mvn compile -q` (또는 해당 모듈만) 로 컴파일 확인. 실패 시 원인 수정 후 재검증.
5. **기록**:
   - 완료한 작업을 `3. 완료 로그`로 이동 (날짜 + 변경 파일 + 한 줄 요약).
   - 작업 중 발견한 새 이슈는 `4. 발견 이슈`에 추가 (임의 수정 금지).
   - 설계 판단이 필요해 멈춘 경우 해당 작업에 `⛔ 결정필요`와 질문을 남긴다.
6. **반복**: 다음 작업으로 진행하거나, `⛔` 항목만 남으면 사용자에게 질문 목록을 보고하고 중단.

**커밋은 사용자가 요청할 때만 수행한다.**

---

## 1. 프로젝트 현황 요약

- Maven 멀티모듈: `core`(도메인) / `item`(Nexo 어댑터) / `mob`(MythicMobs 어댑터) / `enchant`(인챈트 강화 GUI), Java 25, Paper API 26.1.2
- 완성도 높은 부분: Attribute 3-컨테이너, 데미지 Modifier 파이프라인, Buff 생명주기, Gear 조합 스킬 노드 실행
- 진행 중: 스킬 팩토리 등록(실행 순서 미확정으로 주석), 속성(Elemental) 효과, 소환/발사체 스킬, 장비 시스템
- 디버그용 `sendLog` 다수 존재 → **의도된 것, 지우지 말 것** (추후 사용자가 일괄 제거 예정)

---

## 2. 작업 큐 (우선순위 순)

### P1 — 버그 수정 (완료)

*(T1~T4 완료 → 3. 완료 로그 참조)*

### P2 — 미완성 시스템 구현 (기존 구조 준수)

- [x] ~~T5. 버프 생명주기 리스너 연결~~ (완료)
- [x] ~~T6. 자원 리젠 스케줄러~~ (완료)
- [x] ~~T7. 속성 디버프 — 감전(THUNDER)~~ (완료: SHOCK 디버프 + 연쇄 + 15% 추가피해)
- [x] ~~T8. 속성 디버프 — 화상(FIRE)/침묵(WATER)/파쇄(LAND)~~ (완료. WIND는 ⛔ 밸런스 결정 대기, ICE는 명세 미작성)
- [x] ~~T9. 속성 데미지 Modifier~~ (완료: ElementalAtk/DefModifier, LAND→EARTH_* 매핑은 ElementalType에 명시)
- [x] ~~T10. 흡혈(VAMFIRE) 후처리~~ (완료: VamfirePostProcessor, 공식은 todo 확정 필요)
- [x] ~~T11-a. 회피(DODGE) 판정~~ (완료: 확정 공식으로 DamageProcess 초입 판정. 독/화상 회피 제외 여부 todo)
- [x] ~~T11-b. 막기(BLOCK) 판정~~ (완료: 확률 = Block%×(1+(MUL-DIV)/100), 성공 시 무효. 회피→막기 순서는 todo 확인)
- [x] ~~T12. 캐스팅 타임 처리~~ (완료: CastingManager + INVINCIBLE 버프 + 이동차단 리스너 + 피격취소 훅. 비용/쿨은 완료 시 적용. ⚠️ castingTime 단위는 초로 가정 — 확인 필요)
- [x] ~~T13. 발사체 스킬 Effect~~ (완료: ProjectileEffect — SINGLE/SPREAD/CIRCLE, 적중→LAST_TARGET_INFO→다음 노드. RAIN/SELF/POINT는 위치 지정 설계 후)
- [x] ~~T14. Condition 구현체~~ (완료 6/6: Stat/무기소지/체력%/자원/타겟존재/버프)
- [x] ~~T15. SkillEngine 안내 메시지 3종~~ (placeholder 적용 완료, 문구/형식 확정은 ⛔ 사용자)

### P3 — 설계 확정 필요 (⛔ 사용자 결정 전 착수 금지)

- [x] ~~T16. MergeEffect.setConversion 구현~~ (완료: 병렬 그룹 변환 + SkillDefinition 생성자에서 setConversions 호출. 중첩 병합은 미정의-일반 기어 취급)
- [ ] ⛔ **T17. 소환 시스템 (SummonEntity 빈 클래스, SummonData 스텁)**
  - 사용자 답변 "스탯 상속 : En" — **의미 확인 필요.** AI/수명 관리, EffectType.SUMMON 연계 구조도 미정의.
- [x] ~~T18. Controller 추상 클래스~~ (완료: 틱 단위 스킬 내부 개념의 공통 부모로 구현, ProjectileController가 상속. 버프/디버프 제외)
- [x] ~~T19. 장비 시스템~~ (완료: 장비 GUI 7슬롯 + 무기 주손 스캔 + Armor/Weapon/Accessory Mechanic. SUB_WEAPON은 미정 — null 유지)
- [x] ~~T20. SkillEngine.setFactories() 호출 시점~~ (완료: onEnable 초입 호출, 전체 팩토리 등록)
- [ ] ⛔ **T21. 정신(MENTAL) 데미지 타입** — 미정. **설계 금지.**
- [x] ~~T22. IItemModule 인터페이스 정의~~ (완료: getItem(String)/getItem(ItemStack) + ItemModule 구현체 + core 위임 함수. namespacekey 파라미터 형태는 확인 예정)

---

## 2.5 확정된 구조 결정 (2026-07-25 사용자 확인)

1. **데미지 후 부수효과(속성 디버프/흡혈) 훅**: `DamageProcess` 내부 명시적 후처리 단계 (이벤트 리스너/Modifier 방식 아님)
2. **발사체 Effect 적중 처리**: 적중 시 `LAST_TARGET_INFO` 저장 → 다음 노드 실행 (데미지는 후속 기어 담당)
3. **Condition 구현체 6종**: Stat 수치 조건, 무기 소지 조건, 체력 % 조건, 자원 보유 조건, 타겟 존재 조건, 버프 보유 조건
4. **속성 디버프(감전/침묵/파쇄 등) 관리**: 기존 Buff 시스템(BuffType/BuffManager) 재사용

### Attribute 처리 규칙 (2차 확정)
- `MULTIPLY` / `DIVIDE` / `MULTIPLY_REDUCE` / `RESISTANCE` / `RESISTANCE_REDUCE` → **배율 % 처리**
- **회피 공식**: `최종받는데미지 - Dodge * ((DODGE_MULTIPLY - DODGE_DIVIDE) / 100) < random(0, Dodge)` 이면 회피 성공. Dodge 자체는 배율이 아닌 원시값.
- **막기 공식** (3차 확정): 확률(%) = `Block * (1 + (BLOCK_MULTIPLY - BLOCK_DIVIDE) / 100)`, Block 자체가 %값. **성공 시 데미지 완전 무효.**

### 캐스팅 규칙 (2차 확정)
1. **공격받을 시 취소** — 단, 무적 버프(INVINCIBLE, 신규 필요) 상태면 예외. 디버프로 받는 데미지(독/화상)는 취소시키지 않음 — **오로지 상대 엔티티의 공격에 의한 데미지만** 취소.
2. **다른 스킬 사용 시**: 기존 캐스팅 취소 후 새 스킬 작동.
3. **캐스팅 중 이동 불가**: 구속버프 방식이 아닌 MoveEvent 취소로 구현.
4. **비용/쿨타임은 캐스팅 완료 시 적용** (3차 확정 — 취소 시 손실 없음).

### 기타 확정 (2차)
- **T16 MergeEffect**: MergeNum = 한번에 합치는 기어 수. k번째 기어가 Merge(N)이면 k+1..k+N 기어를 **하나의 병렬 노드 그룹으로 동시 실행**, 그 다음 기어는 그룹의 **마지막 기어 이후** 체인 실행.
- **T18 Controller**: ProjectileController의 상위 부모. 이후 틱 단위로 처리되는 스킬 내부 개념(소환수, 장판 등) 전부의 부모. **버프/디버프는 제외** (BuffData 자체 타이머 유지).
- **T22 IItemModule**: 아이템 데이터 구현은 item 모듈 담당. core에는 아이템을 가져오는 조회 함수 제공 (namespacekey 기반).
- **환경 데미지**(낙하/익사 등): UW 파이프라인에서 예외처리 (바닐라 유지).
- **T17 소환** (3차 확정): 소환수는 **고유 스탯 + 시전자(플레이어) 스탯 비례 증가**. 미스틱몹 기준 — 미스틱몹 내부 스탯값이 기본 고유 스탯이며 플레이어 스탯만큼 비례해 증가. 미스틱몹이 아닌 소환수는 스탯 없음. (⛔ 비례 계수/소환 이펙트 파라미터는 확인 필요)
- **T19 장비** (4차 최종 확정):
  1. 슬롯: 갑옷(헬멧/갑옷/레깅스/부츠) + 장신구(목걸이 1, 반지 2) = GUI 7슬롯
  2. GUI 열기: `/equipment`, `/equip`, `/eq`
  3. 저장: 플레이어 A_DataMap (`equipment_data`)
  4. 무기: **주손만** 주기 스캔. 왼손 서브무기는 미정 — 추후 확장 가능하게만 제작
  5. attribute는 Nexo YAML `attributes:` 섹션으로 정의
  6. 갑옷 고유 방어값 `defense:` → ALL_DEFENSE 대응, 무기 고유 공격값 `damage:` → ALL_DAMAGE 대응 (attributes와 **별도 키**로 정의)
  7. 장신구는 고유 고정 스탯 없음 (attributes만)
- **T20 (5차 확정)**: `SkillEngine.setFactories()`는 **onEnable 초입에서 호출** (Gear 아이템이 Nexo 로드 시 팩토리 참조 → core가 먼저 켜지므로 순서 보장).
- **T21 미정 — 설계 금지 유지.**

### 인챈트 강화 (enchant 모듈, 사용자 구현분 — 규칙 고정)
- 성공률 = `100 - (장비 기존 인챈트 개수 × 20) - ((인챈트북 인챈트 개수 - 1) × 10)`, 0 미만 불가
- 결과 인챈트 총개수(기존 ∪ 신규) ≥ 4 부터 실패 시 파괴 확률 = `(결과 총개수 - 3) × 10%`
- 동일 종류 인챈트는 레벨 무관 항상 덮어쓰기 허용
- 장비칸은 인챈트 가능한 장비만(책/인챈트책 제외), 인챈트북칸은 바닐라 인챈트북만
- **이 수치를 임의로 변경하지 말 것**

### 스킬 테스트 시스템 (5차 확정)
- **스킬 제작**: 기어 순서 배치형 GUI (`/skillcraft <이름>`) — 배치 순서 = 노드 실행 순서, 확인 버튼 → 검증(Power≤9) → 저장.
- **스킬 저장/발동**: 별도 슬롯 시스템 — **우클릭 / 쉬프트+좌클릭 / 쉬프트+우클릭 3슬롯** (추후 확장 가능하게), **무기(WeaponItem)를 주손에 들고 사용**.
- **테스트 Effect**: 최대한 다양하게 — 화살 발사, 낙뢰(기존 thunder), 검기(레드스톤 파티클), 타겟 탐색, 단순 데미지, 힐, 버프/디버프 부여.
- Gear YAML costs 스키마: `costs.<타입명>.value` (conditions와 동일하게 키=타입명)로 통일 — 기존 `getString(key)` 방식은 동작 불가라 교체.

---

## 3. 완료 로그

| 날짜 | 작업 | 파일 | 요약 |
|------|------|------|------|
| ~2026-07-25 | (사용자) 치명타 공식 수정 | CombatManager.java | `criMul - criDiv`로 부호 교정 |
| ~2026-07-25 | (사용자) CostFactory 키 타입 수정 | SkillEngine.java | `costMap.get(type)` |
| ~2026-07-25 | (사용자) MythicMob BUFF 컨테이너 합산 | MythicAttributeManager.java | EQUIPMENT 제외 합산 |
| ~2026-07-25 | (사용자) EffectType.TARGET 추가 | EffectType.java, ThunderEffect.java, SimpleModifierEffect.java | 타입 보강 |
| 2026-07-25 | T0. Faction switch 컴파일 에러 수정 | Faction.java | FRIENDLY/NEUTRAL/NONE 케이스 추가 (임시 판정 + TODO, 관계 시스템 설계 대기) |
| 2026-07-25 | T1. effect 팩토리 조건 반전 수정 | GearItemMechanic.java:34 | `!hasEffectFactory`로 교정 |
| 2026-07-25 | T2. conditions/costs null 가드 | GearItemMechanic.java | 섹션 미정의 기어 허용 |
| 2026-07-25 | T3. sumCosts 타입 검사 방향 수정 | ManaCost/HealthCost/StaminaCost.java | `instanceof`로 교정 |
| 2026-07-25 | T4. BuffData.isOffline 타입 수정 | BuffData.java | `A_Player.isOnline()` 사용 (javap로 API 확인) |
| 2026-07-25 | T5. 버프 생명주기 리스너 | BuffLifecycleListener.java (신규), UndefinedWorldCorePlugin.java | 죽음/퇴장/입장 → BuffManager 훅 연결, onEnable 등록 |
| 2026-07-25 | T6. 자원 리젠 스케줄러 | ResourceRegenTask.java (신규), config.yml, UndefinedWorldCorePlugin.java | REGEN-REGEN_REDUCE 주기 적용, RegenSetting.periodTicks(기본 20틱) |
| 2026-07-25 | T15. 스킬 안내 메시지 placeholder | SkillEngine.java | 쿨타임/비용/조건 메시지 3종 (문구 확정 대기) |
| 2026-07-25 | T7. 감전(THUNDER) | ShockDebuff.java(신규), BuffType.java, ShockedDefModifier.java(신규), ElementalPostProcessor.java(신규), DamageProcess.java, DamageModifierBus.java | 감전 부여/연쇄(1칸, 50%, 면역 2초)/+15% 추가피해. DamageProcess 후처리 훅 신설 |
| 2026-07-25 | T8. 화상/침묵/파쇄 | BurnDebuff·SilenceDebuff·ShatterDebuff.java(신규), BuffType.java, ShatterDefModifier.java(신규), ElementalPostProcessor.java, SkillEngine.java | FIRE 도트(체력 0.5%), WATER 5% 침묵(3초)+스킬차단, LAND 중첩 피해증가. BURNING 재부여 가드 |
| 2026-07-25 | T9. 속성 데미지 Modifier | ElementalType.java, ElementalAtk/DefModifier.java(신규), DamageModifierBus.java | 공통 ELEMENT_* + 개별속성 합산. 우선순위 재정렬(속성공격 5, 치명타 1→10, 속성방어 105) |
| 2026-07-25 | T10. 흡혈 후처리 | VamfirePostProcessor.java(신규), DamageProcess.java | isVamfire 타입 한정, VAMFIRE/증폭/저항 반영 (공식 todo) |
| 2026-07-25 | T13. 발사체 Effect | ProjectileEffect.java(신규) | SINGLE/SPREAD/CIRCLE, NORMAL/PIERCE/GUIDED, 전체 종료 시 LAST_TARGET_INFO 확정 |
| 2026-07-25 | T14. Condition 5종 | StatCondition·HealthCondition·ResourceCondition·TargetExistCondition·BuffCondition.java(신규) | 무기 소지 조건은 T22 대기 |
| 2026-07-25 | T18. Controller 부모화 | Controller.java, ProjectileController.java, ProjectileEffect.java | 틱 처리 공통 부모 (start/stop/expire 구분), 발사체가 상속 |
| 2026-07-25 | T22. 아이템 조회 창구 | IItemModule.java, ItemModule.java(신규), UndefinedWorldItemPlugin.java, UndefinedWorldCore.java | getItem(String/ItemStack), Nexo 팩토리 위임 |
| 2026-07-25 | T16. MergeEffect | MergeEffect.java, SkillDefinition.java | 병렬 그룹 노드 재구성(멱등), 마지막 기어만 체인. setConversions 호출 연결 |
| 2026-07-25 | T11-a. 회피 판정 | CombatManager.java, DamageProcess.java | 확정 공식 적용, 회피 성공 시 데미지 무효 |
| 2026-07-25 | 환경 데미지 예외 | EntityDamageListener.java | 낙하/익사 등 비엔티티 데미지는 바닐라 유지 |
| 2026-07-25 | PoisonBuff 타입 정리 | PoisonBuff.java | 바닐라 damage() → UW POISON 타입 (캐스팅 취소 규칙의 타입 판별 대비) |
| 2026-07-25 | T11-b. 막기 판정 | CombatManager.java, DamageProcess.java | 확정 공식, 성공 시 무효. 회피 다음 순서 |
| 2026-07-25 | T12. 캐스팅 타임 | CastingManager.java(신규), CastingMoveListener.java(신규), InvincibleBuff.java(신규), BuffType.java, SkillEngine.java, DamageProcess.java, UndefinedWorldCorePlugin.java | 완료 시 비용/쿨 적용, 피격 취소(독/화상 제외, 무적 예외), 이동 차단, 스킬 재사용 시 기존 캐스팅 취소 |
| 2026-07-25 | T19. 장비 시스템 (core) | WeaponItem·AccessoryItem.java(신규), EquipmentType.java(분류 메서드), EquipSlot·EquipmentManager·EquipmentGUI·EquipmentGUIListener·WeaponScanTask.java(신규), EquipmentCommand.java(신규), StaticValue.java, plugin.yml, config.yml, UndefinedWorldCore.java(null 가드) | 7슬롯 GUI + 주손 무기 스캔(10틱, config) → EQUIPMENT 컨테이너 재계산 |
| 2026-07-25 | T19. 장비 시스템 (item) | ArmorItemMechanic·WeaponItemMechanic·AccessoryItemMechanic.java(신규), U_ItemMechanic.java(parseAttributes), U_ItemMechanicFactory.java | YAML: slot/defense/damage/attributes 파싱, 슬롯 분류 검증 |
| 2026-07-25 | T14. 무기 소지 조건 | WeaponCondition.java(신규) | 주손 아이템 코드 일치 판정 (T22 완료로 차단 해제) |
| 2026-07-25 | 테스트 Effect 6종 | TargetEffect·DamageEffect·HealEffect·BuffEffect·ArrowEffect·SwordAuraEffect.java(신규), ProjectileController.java(moveVisual 훅) | 타겟탐색/데미지/힐/버프/화살/검기(DUST 파티클) |
| 2026-07-25 | T20. 팩토리 전체 등록 | FunctionFactory.java(신규), SkillEngine.java, UndefinedWorldCorePlugin.java | Effect 12종·Condition 6종·Cost 3종 등록, onEnable 초입 호출 |
| 2026-07-25 | 스킬 슬롯 시스템 | SkillSlot·PlayerSkillManager·SkillCastListener.java(신규), StaticValue.java | 3슬롯(확장가능), 기어 코드로 저장→시전 시 조립, 무기 들고 발동 |
| 2026-07-25 | 스킬 제작 GUI | SkillCraftGUI·SkillCraftGUIListener.java(신규), SkillCommand·SkillCraftCommand.java(신규), plugin.yml | 기어 9칸 순서 배치 + 확인 버튼, /skill(list·slots·equip·unequip·cast), /skillcraft(sc) |
| 2026-07-25 | Gear costs 스키마 통일 | GearItemMechanic.java | costs.<타입명>.value 방식 (기존 코드는 동작 불가) |
| 2026-07-25 | 테스트 아이템 22종 + 지급 명령어 | test_items.yml(신규), UGiveCommand.java(신규), UndefinedWorldItemPlugin.java, item/plugin.yml | 무기2·갑옷4·장신구2·기어14, /ugive(ug), Nexo items 폴더 자동 설치(미존재 시) |
| 2026-07-25 | 🔴 메커니즘 미등록 버그 수정 | U_ItemMechanicFactory.java | parse에서 addToImplemented 누락 → getMechanic 항상 null (기어 배치/장비 GUI/무기 스캔 전부 실패 원인) |
| 2026-07-25 | 제작 GUI 기어 쉬프트클릭 허용 | SkillCraftGUIListener.java | 하단 인벤에서 기어만 쉬프트 이동 허용, 비기어는 차단 유지 |
| 2026-07-30 | 검증 스크립트 리포지토리 편입 | tools/compile-check.ps1(신규) | enchant 모듈 포함, `<target> [tag]` 인자로 병렬 실행 격리 |
| 2026-07-30 | 🔴 BuffManager 정적 초기화 결함 | UndefinedWorldCore.java, UndefinedWorldCorePlugin.java | 클래스 초기화 시 plugin instance가 null일 수 있어 스케줄러 등록 실패 → 지연 생성으로 변경. onDisable에서 shutdown() 호출(기존 미호출 dead code) + 모듈 등록 해제로 재활성화 시 이전 plugin 참조 방지 |
| 2026-07-30 | mobModule null 가드 일관화 | UndefinedWorldCore.java | isMythicMob/isDamageableMob는 false 반환(getItem과 동일 정책), getAttributeManager는 조용한 0 폴백 대신 IllegalStateException(원인 추적 우선) |
| 2026-07-30 | registerModule 추적 로그 | UndefinedWorldCore.java | 등록 성공/미지원 타입 무시를 로그로 노출 (기존엔 조용히 무실행) |
| 2026-07-30 | sendLog null 안전화 | UndefinedWorldCorePlugin.java | instance 미설정 시 Bukkit 로거로 폴백 (로그 호출이 NPE를 유발하지 않도록) |
| 2026-07-30 | 🔴 /stat 권한 누락 | core plugin.yml | 일반 유저가 타인 스탯을 set/add 가능했던 문제 → permission: op (사용자가 /ugive, /u_enchant에 쓴 패턴 준수). 전 명령어에 usage/description 추가 |

---

## 2.6 확정된 구조 결정 (2026-07-30, 6차) — **이 항목들이 이전 확정보다 우선한다**

### A_DataMap 사용 원칙 (전역)
`A_DataMap`은 **널포인터가 발생하지 않게 하려고 만든 맵**이다. 조회 시 기본값을 넣어 반환하는 동작은 의도된 설계다.
따라서 null을 기대하는 코드를 쓰지 말고, **존재 여부가 의미를 갖는 곳은 `containsKey`로 먼저 판정**한 뒤 get/set/put 한다.

### 버프 구조 개편
1. **BUFF 컨테이너는 메모리 저장으로 분리** (영속 금지). 서버가 꺼졌다 켜지면 버프는 그냥 사라진다.
2. **중첩용 인터페이스 신설** — 중첩 가능한 버프만 이 인터페이스를 구현한다. 중첩 버프가 아니면 재적용 시 **덮어쓰기**(시간·레벨 전부 교체).
3. **`Buff.getName()` 추가** — 모든 버프는 고유 이름을 갖는다. 같은 버프 클래스여도 **이름이 다르면 다른 버프로 취급**한다(타입으로 기능을 묶되 이름으로 구분). 중첩 버프도 **이름이 같아야 중첩된다.**
4. **중첩 카운트는 레벨로 처리.** 일반 버프는 최대 레벨 없음. 중첩 버프는 `maxStack()`을 두어 그 값을 넘지 않게 한다. (= `BuffContext.level` 상한 문제 해결)
5. 버프 적용/만료 안내는 **플레이어 채팅창**에 출력 (문구는 추후 확정).
6. 버프 적용/추적 로그는 현행 유지.
7. `BurnDebuff` 데미지는 **최대 체력 비례**가 맞다 (현행 유지).

### 아이템 · 장비
1. **장비 로어는 Nexo에서 구현한다 — 플러그인에서 만들지 말 것.**
2. **`HEALTH_MAX` 반영 공식: `max * (1 + mul - div)`**
3. PAPI 수치는 **소수점 한 자리**까지 표기.
4. `/ugive` 수량 상한 **1000**.
5. **더블클릭 스택 모으기를 허용한다 — 단 하단(플레이어) 인벤토리 안에서만.** GUI 상단은 긁히지 않게 해 복사를 차단한다.
   또한 **CustomGui를 쓰지 않는 GUI들은 CustomGui를 쓰도록 다시 만든다.**
6. GUI 안내 아이템·디자인은 임의 제작 후 추후 변경.

### GUI · 인챈트
1. **스킬 개수 상한 없음.** 대신 **제작하면 완성된 스킬 아이템 1개로 제공**한다. 이 아이템은 **수정·확인이 가능해야 한다.**
   발동은 **기존대로 슬롯에 장착해서 사용**한다(무기 들고 우클릭/쉬프트클릭 3슬롯 유지). 이름 길이 상한은 현행 유지.
2. `/skill delete`는 추가하지 않는다 (아이템이므로 불필요).
3. `/skill cast`는 **op 전용**.
4. `/u_enchant`는 **op 전용 유지**.
5. **강화 비용은 넣지 않는다** (인챈트북 1권 소모만).
6. **제작 시 기어가 소모된다.** 단 **수정 GUI에서 기어로 다시 꺼낼 수 있어야 한다.**
7. `castingTime`은 내부적으로 틱이되 **플레이어에게는 초로 표기**.

### 스킬 코어
1. **캐스팅은 틱 단위로 처리, 플레이어 표기는 초.**
2. **캐스팅 중 침묵이 부여되면 캐스팅 취소.**
3. **탈것 탑승 중에는 스킬 사용 자체를 차단.** 캐스팅 중 탈것에 타면 캐스팅 취소.
4. **캐스팅 중 텔레포트되면 캐스팅 취소.**
5. **노드 체인 도중 시전자가 사망/퇴장하면 남은 노드를 즉시 중단.**
6. 기어 파워는 **총합 9 이하이기만 하면 된다** (음수 허용).
7. **비용·쿨타임은 캐스팅 완료 시 적용** (§2.5 유지 — 취소되면 손실 없음).
8. 비리빙 엔티티가 체력 관련 스킬을 못 쓰는 것은 **의도된 동작**.

---

### 7차 세션 도메인별 완료 현황
| 도메인 | 패스 | 수정 | 상태 |
|---|---|---|---|
| 데미지 파이프라인 | 9 | 26건 (치명 4/높음 5/보통 12/낮음 5) | ✅ 완료 |
| 스킬 코어·캐스팅 | 5 | 14건 (치명 2/높음 3/보통 4/낮음 5) | ✅ 완료 |
| 이펙트·타겟·발사체 | 7 | 20건 (높음 6/보통 9/낮음 5) | ✅ 완료 |
| 버프 구조 개편 | 4 | 7건 + §2.6 전 항목 구현 | ✅ 완료 |
| GUI·스킬 아이템 | 5 | 10건 (치명 1/높음 2/보통 5/낮음 2) | ✅ 완료 |

**GUI 도메인 주요 성과**
- 🔴 **모든 UW 아이템의 Nexo YAML 로어가 지워지고 있었다** — `U_ItemMechanic`이 로어 ItemModifier를 등록하는데 `setLore()`가 호출되지 않아 빈 리스트로 덮어썼다. Nexo가 YAML 로어를 채운 뒤 메커니즘 modifier가 실행되는 순서를 바이트코드로 확인. §2.6 "로어는 Nexo 담당"과 정면 충돌하던 상태.
- **스킬 아이템화 완료**: 재질 `ENCHANTED_BOOK`(최대 스택 1이라 겹침 원천 불가), PDC가 SSOT(`LIST.strings()`로 구분자 문제 회피), 확인은 아이템 로어, 수정은 들고 우클릭(아이템을 손에서 회수한 뒤 기어 전개 → 동시 존재 상태 없음), 슬롯이 아이템 실물 보관.
- **`U_Gui` 공용 베이스 신설** — 3종 GUI의 클릭 차단 정책이 정확히 한 곳에만 존재. 더블클릭은 하단 36칸만 직접 수집(상단 절대 미접근).
- 일반 플레이어가 도달 가능한 **복사 경로 0** (제작/수정/장착/해제/닫힘/드롭/사망/종료/더블클릭/드래그/숫자키/오프핸드/타인조작/이중오픈 전수 추적).

**오케스트레이터 마무리 배선**: `SilenceCastListener` 신설(버프 이벤트 구독 → 캐스팅 취소, 순환 의존 없이), `closeOpenGuis()`를 `U_Gui.handleClose()` 단일 경로로 통일, `/skill` usage 갱신.

**오케스트레이터 직접 수정**: `CTXType.TARGET_COUNT`의 기본값 1 제거.
`SkillCTX` 생성자가 기본값 있는 CTXType을 전부 채우므로 `hasCTX`가 항상 true가 되어 "CTX가 없으면 YAML" 폴백이 성립하지 않았다.
그 결과 `gear_target`의 `count: 3`이 무시되고 항상 1명만 탐색됐다. 소비처 2곳은 이미 명시적 기본값을 쓰는 2인자 `getCTX`라 안전.

**⛔ 새로 발견된 구조 문제 — 패키지 순환 의존**
`skill` → `combat.buff`(`CastingManager`/`SkillEngine`이 `BuffManager`·`BuffType` 사용)는 원래 있었고,
침묵 취소 배선으로 `combat.buff` → `skill`(`CastingManager.onSilenced`)이 추가되어 **import 수준 양방향**이 됐다.
CLAUDE.md "의존성 단방향 유지, 순환참조 금지"에 저촉된다. 해소하려면 버프 생명주기 이벤트/리스너 같은 새 구조가 필요하므로 **사용자 결정 대기**.

**7차에서 드러난 치명 결함 (기능이 아예 동작하지 않던 것)**
- `pierce_increase` 기어는 팩토리 타입 불일치(`double.class` ↔ `(int)` 생성자)로 **로드 자체가 실패**했다.
- `castingTime`에 `*20`을 곱해 **`cast: 3`이 60초 캐스팅**이 됐다.
- 발사기 화살이 플레이어를 맞히면 `@NotNull` 위반 NPE로 **데미지가 통째로 소실**됐다.
- 미스틱몹은 Stat 하나만 미등록이어도 조회 단계에서 예외 → **그 몹이 무적**(타입 150개라 사실상 항상 재현).
- 발사체 사거리 판정이 적중 판정보다 먼저라 **마지막 이동 구간의 적을 항상 놓쳤고**, speed ≥ range면 적중 판정을 한 번도 못 했다.
- `ThunderEffect` 치명타 이중 판정으로 실효 확률이 30% → 51%로 부풀었다.

## 2.12 12차 세션 완료 현황 (2026-07-30)

| 도메인 | 패스 | 결과 |
|---|---|---|
| 명령어·스킬 장착 GUI | 3 | ✅ `/skill craft` 통합, 장착 GUI 신설, `unequip` 폐지, 이름 검증(유니코드 문자·숫자) |
| CTX 수정자 이펙트 | 4 | ✅ **수정자 17종 신규** + 팩토리 20줄 등록, 회복량 DAMAGE 배율 |
| 디버그 모드 | 4 | ✅ `/skill debug` — 기어 실행·조건 통과·캐스팅 진행률·취소 사유 |
| 무적·버프 안내·소환 | 5 | ✅ 무적 데미지 무효, 버프 시작/종료 안내, 소환수 스탯(고유 + 시전자 10% 스냅샷) |

**추가 확정**
- `LOOK` 모드 레이캐스트 거리 **상한 없음** — 기어 YAML 배율값으로 운영 통제 (곱셈 폭주 결정과 동일 기조).
- 무적 안내는 회피/막기 관례대로 **매번** 출력(빈도 조정은 추후).
- 버프 종료 안내는 **EXPIRED + FORCED만** — `QUIT`은 도달조차 안 하고 `DEATH`는 버프 수만큼 도배되므로 제외(담당 판단).

**남은 미완**
- `EffectList.md` 가 비어 있어 **목록 기반 이펙트 추가 미착수**.
- `SEARCH_AREA` 수정자 보류(YAML 스키마 미정), `CASTER`/`LAST_TARGET_INFO` 는 의도적 미제작.
- **소환수 스탯이 실제 전투에 반영되지 않는다** — 데미지 파이프라인이 `SummonEntity` 를 거치지 않는다. 배선하려면 미확정인 소환 수명 관리가 필요.
- `REPEAT`·`CHAIN`·`RENDER` 는 소비처가 없어 수정자를 만들어도 효과 없음(주석 명시).
- ⚠️ `size_increase` → `size_multiply` 로 **연산 의미가 바뀌었다.** 기존 기어 YAML 에 있으면 값 재조정 필요.

---

## 2.11.5 🔴 검증 스크립트 결함 (2026-07-30, 이펙트 담당 발견)

**`tools/compile-check.ps1` 이 컴파일 에러를 놓치고 BUILD SUCCESS 를 내고 있었다.**

원인: classpath 에 로컬 Maven 저장소 전체 jar 를 넣었는데, 그 안에 **이 프로젝트 자신의 설치본**
(`repository/org/red/minecraft/uw/{core,item,mob,enchant}-1.0-SNAPSHOT.jar`, 2026-07-30 18:08 빌드)이 있었다.
javac 가 소스에서 못 찾은 심볼을 **낡은 jar 에서 찾아 해결**해버려 다음이 전부 통과했다:
- 클래스 삭제·이름 변경
- 메서드/생성자 시그니처 변경 (호출부가 낡은 시그니처로 통과)
- 패키지 이동 (구 위치가 jar 에 남아 있음)

→ classpath 필터에 `repository\org\red\minecraft\uw\` 제외를 추가해 수정했다(`dellarte` 는 진짜 외부 의존성이라 유지).
→ **오염 제거 후 재검증 결과 숨어 있던 실제 에러는 1건뿐**(삭제된 `SizeIncreaseEffect` 참조)이었고, 이를 고쳐 4개 모듈 전부 통과를 확인했다.
→ **이후 "BUILD SUCCESS" 판정은 이 수정본 기준으로만 신뢰할 것.**

---

## 2.11 확정 (11차, 2026-07-30 — 에이전트 질문 답변)

### 스킬 이름
- **공백 불가.** `/skill craft <이름>` 은 인자가 2개 이상이면 거절.
- **문자 범위는 유니코드 전반** — 유니코드상 문자(letter) 또는 숫자(digit)면 허용, 기호만 거절. (ASCII 한정 아님. 한글도 유니코드 문자라 따로 판정할 필요 없음)
- **규칙 이전에 만든 스킬 아이템**은 수정 GUI 를 **아예 열지 않는다**(아이템 회수 전에 이름 검증). 아이템 보존 최우선.

### 무적
- **무효 대상에서 `POISON`/`BURNING`/`COST` 를 제외**한다. 도트 제외는 캐스팅 취소 규칙과 같은 결, COST 제외는 무적 중 체력비용 스킬 무한 무료 시전 악용 차단.
- 판정 순서는 **회피/막기보다 앞**. (확정 무효라 이중 판정 방지 / 회피·막기는 `hasAttacker` 한정 / 데미지 값 불필요)
- ⚠️ `BURNING` 은 `canDeath=true` 라 **무적 중에도 화상으로 죽는다** — 의도된 동작.

### CTX 수정자 연산 규칙 (전역)
**`CTXType` 의 선언 타입으로 가른다:**
- **`double.class` → 곱셈(배율)**
- **`int.class` → 덧셈**
- 열거형·객체 → 덮어쓰기

⚠️ **지수 폭주는 인지된 상태로 수용된 결정이다 (재확인 완료).**
기어는 플레이어가 최대 9개 조합하는 아이템이고 같은 수정자를 여러 개 넣는 것을 막는 장치가 없다.
배율 수정자가 중첩되면 곱해지므로 `damage_multiply: 3` 기어 9개 = **19,683배**, `range_multiply: 5` 9개 = **약 195만 배**(발사체가 사실상 무한히 날아 서버 정지급)가 된다.
→ **통제 수단은 서버가 설정하는 기어 YAML 의 배율값**이다. 코드에 상한을 넣지 않는다.
→ 무효값 방어(`isTravelable`, `isFinite` 검사 등)는 상한이 아니라 안전장치이므로 **유지**한다.
→ 테스트 기어의 배율값은 보수적으로(1.2~1.5) 잡을 것.
→ `SizeIncreaseEffect`(SIZE, double)는 **덧셈에서 곱셈으로 변경**해야 한다. `PierceIncreaseEffect`(PIERCE, int)는 덧셈 유지.
→ `LEVEL`(int)은 덧셈 수정자. `BuffEffect` 가 그 값을 배율로 소비하는 것은 **소비 측 규약이라 별개 층**이다.

### 수정자 세부 (12차 확정)
- **`TARGET_COUNT` 기본값 = 0** (덧셈의 항등원). 1로 두면 수정자가 없어도 항상 +1 이 되어 소비처마다 보정이 필요하다.
  소비처는 **`기어 YAML 값 + CTX`** 로 쓴다. **절대 개수로 쓰면 안 된다**(기본값 0이라 대상이 0이 된다).
- **`SEARCH_RANGE` 는 절대값 유지** — 기본값 없는 덮어쓰기 수정자. 탐색 반경 **배율은 기존 `RANGE` 하나로 통일**한다(둘 다 두면 배율 수정자가 중복).
- `size_increase`(덧셈) → **`size_multiply`(곱셈)** 으로 교체됨. **연산 의미가 바뀐 것이라 기존 기어 YAML 값 재조정이 필요하다.**

### 수정자 미제작
- **`SEARCH_AREA` 는 보류** (YAML 스키마 정의 필요).
- **`SEARCH_CENTER` 는 제작** — 좌표가 아니라 **동작 지정**(시전자 위치 / 직전 타겟 위치 / 바라보는 지점). 실행 시점에 위치로 해석해야 한다.
- `CASTER`/`LAST_TARGET_INFO` 는 런타임 상태값이라 수정자를 만들지 않는다(CASTER 덮어쓰기는 시전자 위조 악용 경로).

### 소환 (T17)
- **`SummonEntity` 가 소환된 엔티티 + `SummonData` 를 들고 `AttributeHolder` 를 구현**한다. `SummonData` 는 소환 파라미터 전용.
- **시전자 10% 는 소환 시점 스냅샷** (실시간 아님). 시전자가 버프를 받거나 죽어도 소환수 스탯은 불변.
- `setAttributeValue` 는 **BUFF 컨테이너 위임** (소환수 자체 스탯은 미스틱몹 읽기 전용이므로).
- 소환 이펙트·수명 관리·AI 는 **여전히 미확정 — 만들지 않는다.**

---

## 2.10 확정 (10차, 2026-07-30)

### 스킬 아이템 · GUI
1. 기존 스킬 데이터 — **현행유지**(읽지도 지우지도 않음)
2. 수정 GUI를 확인 없이 닫으면 해체 — **일단 현행유지**
3. **스킬 이름은 한글·영문자·숫자만.** 특수문자 불가(공백 포함 불가로 해석 — 담당이 막히면 질문)
4. **스킬 아이템만 Nexo를 거치지 않는다** (현행 유지 — 나머지 아이템은 Nexo)
5. 스킬 기어 수 최대 9개 — 맞음
6. **스킬 장착을 GUI 방식으로 변경** — `/skill equip` 이 장착 GUI 를 연다
7. **`/skillcraft` 명령어 삭제 → `/skill craft` 로 통합**
8. **`/skill unequip` 삭제** (장착 GUI 에서 처리)

### 전투 · 이펙트
- **`InvincibleBuff` 에 데미지 무효 효과 적용** (지금까지는 캐스팅 취소 예외 마커일 뿐이었다)
- Projectile `CIRCLE` — **현행유지**
- **회복량은 `DAMAGE` CTX 의 영향을 받는다**
- **발사 높이는 스킬마다 다르게** (통일하지 않는다)
- **버프 안내 문구는 임의 제작 — 시작과 종료만**
- `CriticalAtkModifier` 계산 방식 — **현행유지**

### 추가 개발
1. **이펙트 추가** — `EffectList.md` 기반 + **기본적으로 있어야 할 CTX 수정자 이펙트 전부**
   ⚠️ **`EffectList.md` 가 비어 있다(0바이트).** 목록 기반 추가는 내용 확정 후 착수.
2. **디버그 모드** — `/skill debug` (op 전용). 켠 상태로 스킬을 쓰면 기어가 어디까지 실행됐는지 / 캐스팅 시간 / 취소 시 얼마나 진행됐고 왜 취소됐는지 / 기어 조건 통과 여부 등 디버깅에 필요한 로그 출력.
3. **T17 소환수 스탯** — **미스틱몹 고유 스탯 + 시전자 Attribute 의 10%**. (여기서 "스탯"은 Attribute)

---

### 9차 마무리 반영 (전 도메인 완료 시점)
- `DamageTypeEffect` 수정자 기어 신설 + `SkillEngine.setFactories()` 등록 (`damage_type_effect`, YAML: `damageType: MAGIC`).
  `DamageEffect`·`SwordAuraEffect`가 이 CTX를 따르고(없으면 기존 값 폴백), `ProjectileEffect`는 무속성 색상 판정에 사용.
  `ThunderEffect`의 `CHAIN_LIGHTING`은 흡혈 불가·고정데미지 플래그를 가진 **낙뢰 메커니즘 자체**라 고정 유지.
- `AttributeBuff` 이름 = `ATTRIBUTE_BUFF@<스탯>@<출처>`.
  구분자를 `_`로 두면 `BLOCK`/`BLOCK_DIVIDE` 같은 **접두사 겹침 쌍 20+개**에서 서로 다른 버프가 같은 키가 되어 `@`를 썼다.
  **`source`는 필수**(기본값 없음) — 스탯 파생은 출처 구분 불가, 매번 고유값은 같은 출처 재적용까지 중첩되어 무한 강화가 된다.
  ⛔ 이 "필수" 판단은 담당 에이전트가 내린 것이므로 사용자 확인 필요(기존 호출자 0건이라 깨지는 곳은 없음).

### 9차 확정 (2026-07-30)
- **`CTXType.DAMAGE_TYPE(DamageType.class)` 신설** — 이 스킬이 주는 데미지 유형을 정한다. 수정자 기어가 세팅하면 **이후 기어들이 그 유형으로 데미지를 준다.** 값이 없을 때를 대비해 **데미지를 주는 이펙트는 각자 기본값을 보유**한다(그래서 CTX에는 기본값을 두지 않는다 — `TARGET_COUNT`와 동일 이유).
  - 발사체 무속성 색상도 이 CTX로 물리(회색)/마법(파랑)을 가른다. 둘 다 없으면 물리 폴백.
- **기존 스킬 데이터(이름→기어코드)**: 그대로 방치 (읽지도 지우지도 않음).
- **수정 GUI를 확인 없이 닫으면 스킬 해체** 유지 (기어는 전부 반환되므로 손실 없음).
- **`AttributeBuff`는 출처별로 따로 유지** — 같은 스탯을 올려도 스킬/포션 등 출처가 다르면 동시 적용된다. (기본 이름 규칙은 담당이 검토 중, 무한 강화 위험이 있어 확정 필요 시 재질의)
- **버프 생명주기 이벤트 패키지 위치**: `core/event` 유지. 제거된 것은 "다른 도메인 동작을 직접 호출"하는 결합이었고, 남은 타입 참조는 정상 계층으로 판단(오케스트레이터 결정).

### 8차 확정 (2026-07-30)
- **패키지 순환 의존 해소**: `combat.buff` → `skill` 직접 호출을 없애고 **버프 생명주기 이벤트를 신설**한다(적용/종료 2종, `UWEvent` 계열 관례 준수, 취소 불가). `skill` 쪽이 구독해 침묵 취소를 처리 → 의존은 `skill` → `combat.buff` 단방향.
- **발사체 블록 충돌**: `PIERCE`는 **통과**, 그 외(`NORMAL`/`GUIDED`)는 블록에 닿으면 **소멸**(`expire()`).
- **발사체 시각효과**: 레드스톤(DUST) 파티클로 통일. **무속성 물리 = 회색, 무속성 마법 = 파란색**, 그 외는 속성에 맞는 색. 색 결정 로직은 한 곳에 모으고 검기도 같은 규칙을 쓴다.
  ⚠️ `ProjectileEffect`는 데미지를 주지 않아 물리/마법 구분 근거가 없다 — 담당이 막히면 질문 예정.
- **미사용 CTX**: `LEVEL`/`TIME`만 `BuffEffect`의 레벨·지속시간에 연결. 나머지 5종(`REPEAT`·`CHAIN`·`RENDER`·`PIERCE`·`SEARCH_AREA`)은 todo 유지(제거 금지).

### 7차 확정 (2026-07-30, 에이전트 질문에 대한 사용자 답변)
- **중첩 버프 레벨**: 매니저가 누적 — `새 레벨 = min(maxStack, 기존레벨 + 요청레벨)`. 호출부는 "추가할 양"만 넘긴다.
- **파쇄 maxStack = 10** (최대 +50%).
- **버프 스냅샷(`buff_suspends`)**: PDC 저장 유지 + **서버 세션 ID**를 함께 저장해 세션이 다르면 폐기. → 재시작 소멸, 같은 세션 재접속은 복원(다중서버 의도 보존).
- **스킬 아이템**: 슬롯이 **아이템 실물을 보관**(장비 GUI와 동일 모델, 복사 원천 차단) / **core가 만드는 바닐라 아이템** / **아이템 PDC가 SSOT**(`skill_data.skills` 폐기) / 확인·수정은 **아이템 들고 우클릭** / op의 복제는 **허용**(일반 유저 경로는 전부 차단).
- **공용 GUI 베이스 클래스 신설 허용** — 3종 GUI 클릭 차단 정책을 한 곳으로.
- **회피/막기 성공 시**: 캐스팅 **취소** + 양쪽에 회피/막기 구분 메시지. 속성 디버프·흡혈은 미발동, 이벤트 미발행 유지.
- **탈것 탑승 중 스킬 차단은 플레이어만** (몹은 탑승해도 사용 가능 — 이동 차단 우회 문제가 플레이어 전용이므로).
- **음수 파워로 쿨타임이 0 이하면 "쿨 없음"** 유지, 최소 쿨타임 없음.
- **미스틱몹 Stat 미등록**: 0 폴백. 단 **`HEALTH_MAX`만 예외로 몹의 현재 체력**을 반환.
- **`scale`은 물리/마법 + 공격자 있을 때 전용** (현행 유지, 문서화만).
- **`randomCriCheck` 배율은 `/100`** (§2.6 배율 % 규칙 적용 — 오케스트레이터 판단).
- **발사 개수(COUNT) 상한 두지 않음** (밸런스로 관리).
- **타겟 탐색은 CTX 우선** — YAML은 CTX가 없을 때의 기본값.

### 6차 확정 반영 현황 (오케스트레이터 직접 처리분)
- ✅ `HEALTH_MAX` 실제 최대 체력 반영 — `PlayerHelper.applyMaxHealth()`, 공식 `(기본체력 + HEALTH_MAX) * (1 + (MUL - DIV)/100)`, 최소 1 클램프, 최대치 축소 시 현재 체력 동반 보정.
  ⚠️ **확인 필요**: 공식의 `max`를 "바닐라 기본 체력 20 + HEALTH_MAX"로 해석했다. HEALTH_MAX만으로 계산하면 HEL 스탯 0인 플레이어의 최대 체력이 0이 되어 즉사하므로 기본 체력을 더했다.
- ✅ PAPI 수치 소수점 한 자리 표기 (`U_PapiPlayer.format`, 정수 계열은 그대로).
- ✅ 장비 로어는 Nexo 담당 — 플러그인에서 생성하지 않음(현 상태 유지, `setLore()` 미호출이 정답).

---

## 3.5 검증 세션 (2026-07-30, 다중 에이전트 엄격 감사)

도메인별 전담 에이전트 6명을 배치해 "감사 → 수정 → 새 눈으로 재감사"를 결함 0까지 반복하는 방식으로 진행.
**API 월간 지출 한도 도달로 세션이 중단됨.** 아래는 확정된 결과와 미완 상태다.

### 완료
- **버프/디버프 도메인**: 7패스 반복, 마지막 패스 결함 0. 결함 18건 수정 (치명 3 / 높음 5 / 보통 7 / 낮음 3).
  주요 수정: `buff_suspends` 무한 누적·중복 복원, 오프라인 플레이어 스냅샷 저장 시 NPE로 인한 스탯 영구 잔존,
  `handleEnd` 예외로 나머지 버프 정리 중단, duration이 tick 주기 배수가 아닐 때 최대 (주기-1)틱 초과 지속(만료 전용 태스크 분리),
  `ended` 검사/설정 분리로 인한 onRemove 이중 실행(스탯 이중 차감) → CAS, `active` 내부 맵 비원자 갱신으로 인한 유령 버프,
  `start()` 실패 시 타이머 없는 버프 잔존 / onApply 원복 누락, `BuffContext` 방어적 복사 미실행 및 조회만으로 맵이 변경되는 side effect,
  `RegenerationBuff`가 최대체력 감소 직후 체력을 깎는 문제, `GlowingBuff`가 외부 발광까지 끄는 문제,
  `Suspended.deserialize` 실패 시 플레이어 데이터 로드 불가(접속 불가) → 건너뛰기 처리.
- **오케스트레이터(공통 진입점/설정)**: 결함 5건 수정 — 완료 로그 참조.
- **빌드 복구**: 중단된 이펙트 담당 에이전트가 `ProjectileController`를 미완성으로 남겨(import 누락 + `isTravelable()` 미정의) core 컴파일이 차단된 상태였음.
  에이전트가 주석으로 남긴 설계 의도(모든 종료 경로를 `expire()`로 모아 onExpire를 발사체 1기당 정확히 1회 보장)에 맞춰 완성:
  `isTravelable()` 구현(speed/range가 0 이하·비유한이면 즉시 수명 종료 — 태스크 영구 잔존 방지), NORMAL 적중 시 `stop()` → `expire()`,
  그에 맞춰 `ProjectileEffect`의 onHit 완료 집계 제거(이중 집계로 여러 발일 때 나머지 발 전에 future가 완료되던 문제).
  → 4개 모듈 전부 컴파일 성공 확인.

- **스킬 엔진·캐스팅 도메인**: 5패스, 마지막 패스 결함 0. 결함 20건 수정 (치명 2 / 높음 5 / 보통 8 / 낮음 5).
  - 🔴 **쿨타임이 20배로 적용되던 문제** — `CoolTimeMap.setCoolTime`의 기본 단위가 초인데 `쿨 * 20`을 넘기고 있었다. 5초 쿨 스킬이 실제로 100초. **쿨타임 단위는 초로 확정.**
  - 🔴 **원시 타입 CTX 조회가 전부 런타임 예외** — `A_DataMap.getClass`가 `Class.cast`를 쓰므로 `double.class.cast(Double)`은 항상 ClassCastException. `CTXType` 선언이 `double.class`라 SEARCH_RANGE/SIZE/DAMAGE 등 모든 수정자 조회가 실패했고, `SimpleModifierEffect`의 타입 검사도 항상 실패해 수정자 Effect 생성 자체가 불가능했다. 생성자에서 원시→래퍼 정규화로 해결.
  - 체력 비용 합산 검사 무력화(다중 기어 합산을 무시해 자살 가능), 비용 지불 실패 시 로그만 남기고 **무료 시전 + 쿨타임 적용**, 캐스팅 중 로그아웃/사망 후 스킬 발동, 사용 불가 스킬 입력만으로 기존 캐스팅 소실, Effect 예외 시 체인이 로그 없이 사망, `A_DataMap.getClass`가 키 부재 시 **null을 삽입**해 이후 `hasCTX`가 true가 되어 기본값 폴백이 깨지는 문제(9곳 영향), 이동 차단이 동일 좌표 월드 이동으로 우회, 팩토리 예외에 기어/경로 정보 부재.
- **GUI·명령어·인챈트 도메인**: 5패스, 마지막 패스 결함 0. 결함 23건 수정.
  - 🔴 **아이템 무한 생성** — GUI 필러와 같은 아이템을 들고 하단 인벤에서 **더블클릭**하면 `COLLECT_TO_CURSOR`가 상단 GUI의 필러/버튼을 전부 커서로 긁어모아 대량 복사된다. 제작 GUI·인챈트 GUI 차단 완료.
  - 🔴 **인챈트 결과물 영구 소실** — 결과칸에 아이템이 남은 상태로 재강화 시 덮어써져 사라진다. 결과칸 점유를 최우선 차단으로 해결.
  - 🔴 **장비 스택 복사** — 장비 64개 스택에 책 1권으로 64개 전부 인챈트. `amount != 1` 차단.
  - 🔴 **닫힘 처리 누락 시 소실** — dellarte의 닫힘 디스패치가 `IgnoreInvClose` 상태면 `CustomGui.onClose`를 호출하지 않는다. 자체 리스너에서 멱등 반환을 병행 호출해 우회.
  - 🔴 **enchant 플러그인 리로드 시 소실** — `onDisable` 훅 신설.
  - `A_DataMap.getString`은 키 부재 시 **`""`를 반환하고 저장**, `getList`는 **빈 리스트를 저장** → 빈 슬롯이 null이 아니게 되어 무기 우클릭마다 메시지 스팸, `/skill cast 아무이름` 한 번으로 그 이름이 보유 스킬로 등록되는 **이름 선점 악용**. 스킬 이름 검증 전무(`.` 포함 시 `A_DataMap.put`이 예외 → 저장 흐름 중단).
  - 편의성: 강화 확률/파괴율 미공개(도박 강요) → 버튼 lore에 표기, 비활성 버튼이 필러와 동일 외형 → 색 구분, 제작 GUI에 파워 n/9·예상 쿨타임 실시간 표기, 슬롯 안내 아이템 추가, 로케일 의존 `toUpperCase` → `Locale.ROOT`.
- **속성·장비·아이템 도메인**: 8패스, 마지막 패스 결함 0.
  - 🔴 **장비 GUI 더블클릭 아이템 복사** — `COLLECT_TO_CURSOR`가 열린 창 전체를 긁어 상단 GUI 장비가 슬롯 검증과 저장을 모두 우회해 커서로 빠져나가고, 저장 데이터에는 그대로 남아 재접속 시 복사된다. `InventoryAction` 전수 점검 결과 **상단↔하단을 넘나드는 동작은 `MOVE_TO_OTHER_INVENTORY`와 `COLLECT_TO_CURSOR` 둘뿐**임을 확인하고 후자를 차단.
  - 🔴 **1틱 지연 저장 창** — 클릭 후 1틱 뒤 저장이라 그 사이 크래시/강제퇴장 시 저장 데이터와 인벤토리가 불일치. 장비 슬롯 클릭을 전부 취소하고 직접 처리해 **같은 틱에 저장**하도록 변경(바닐라 병합·부분이동 규칙 자체를 배제).
  - 🔴 **슬롯 무검증 저장 + 이중 오픈 되돌림** — 어떤 경로로 들어온 아이템이든 그대로 저장했고, GUI가 생성 시점 스냅샷을 들고 있어 두 번 열면 옛 상태가 나중에 저장되어 변경이 되돌아가거나 장비가 복원됐다.
  - `A_DataMap.getItemStack`은 키 부재 시 **AIR를 맵에 써넣고 반환** → 조회만으로 슬롯 7개가 영구 저장되고 `@Nullable` 계약이 깨졌다. `Inventory.getItem`이 슬롯 mirror라 그대로 저장하면 이후 인벤 조작이 저장 데이터를 조용히 변경(양방향 `clone()`으로 해결). 반지 슬롯 64스택 장착(4개 관문 전부 차단으로 해결).
  - `AttributeType` 150개를 0으로 덮어써 DataMap에 150키가 무기 스캔 주기마다 기록되던 문제 → `AttributeManager.clearBaseAttributeValues(ContainerType)` 신설. 최대치 감소 시 현재 마나/스테미나 미보정, `/ugive` 수량 상한 없음(서버 정지 가능), `U_PapiPlayer`가 화면에 `"null"` 표시, YAML 숫자 아닌 값이 조용히 0이 되던 문제(`defense`/`damage`/`cool`/`power`/`cast`) 등 수정.
  - 편의성: 장비 GUI 슬롯 안내 아이템 신설(PDC 표식으로 꺼내기·저장 모두 차단), 장착 실패 이유 전달, 하단 인벤 쉬프트클릭 자동 장착/해제.
- **오케스트레이터 교차 도메인 반영**:
  - 퇴장/사망 시 캐스팅 정리 훅 부재(재접속 후 남은 캐스팅 시간만큼 이동이 막힘) → `CastingLifecycleListener` 신설(`BuffLifecycleListener`와 동일 역할 구조), `CastingManager.cancelCast(UUID)` 오버로드 추가.
  - `onDisable`에서 열려 있는 제작/장비 GUI 정리(아이템 소실 방지).
  - `/skill cast`는 무기 소지 조건을 우회하는 테스트 백도어 → op 제한.
  - `UndefinedWorldCore.hasItemModule()` 신설 — item 모듈 등록 전에는 `getItem`이 "UW 아이템 아님"과 "조회 불가"를 구분할 수 없어 장비 재계산이 스탯을 0으로 밀어버렸다(`/reload` 시 재현). `EquipmentManager.applyEquipmentAttributes`가 이 값을 먼저 확인해 재계산 자체를 건너뛴다.
  - `MythicAttributeManager.clearBaseAttributeValues` 오버라이드 — 미오버라이드 상태로 몹에 호출되면 buffAttributes 대신 엔티티 DataMap을 지운다. 다른 오버라이드와 동일하게 EQUIPMENT/STAT 거부 + BUFF는 자체 맵 clear.

### 중단 (재개 필요)
| 도메인 | 상태 | 비고 |
|--------|------|------|
| 데미지 파이프라인 | 중단 | `DamageModifierBus` 치명타 등록 조건 수정까지 반영됨(공격자 없을 때 CriticalDef만 등록되어 데미지가 일방적으로 깎이던 문제). 마지막으로 `DamageSource`의 `@NotNull` 위반 수정 착수 직전에 종료 |
| 이펙트·발사체 | 중단 | `Controller`/`ProjectileController` 종료 모델 재설계 반영됨(위 빌드 복구 참조). 나머지 Effect 일관성 감사 미완 |
*(속성·장비·아이템 도메인은 완료 — 위 완료 목록 참조)*

### 미감사 영역 (재개 시 최우선)
아래 두 도메인은 **엄격 감사 반복이 끝나지 않았다.** 다른 도메인에서 확인된 함정(`A_DataMap` 조회가 값을 삽입/저장하는 side effect, 원시 타입 캐스팅 실패, GUI 클릭 우회, 정적 맵 누수)이 여기에도 있을 가능성이 높다.
- `combat/CombatManager`, `combat/damage/**`(DamageCTX, DamageSource, DamageType, modify/*, process/*), `event/**`, `mob/**`
- `skill/effect/**`(ThunderEffect·DamageEffect·HealEffect·BuffEffect·ArrowEffect·SwordAuraEffect·TargetEffect·수정자 계열), `skill/target/**`, `skill/summon/**`
  - 특히 `DamageSource`의 `@NotNull` 위반(데미지 담당이 착수 직전 중단), Effect 8종의 `LAST_TARGET_INFO` 처리·죽은 엔티티 필터링·CTX 배율 반영 일관성

### 추가 설계 결정 대기 (속성·장비 도메인 제기)
- **`U_ItemMechanic.setLore()`가 어디서도 호출되지 않는다** → 모든 UW 아이템에 로어가 없고 방어/공격/attribute 수치가 표시되지 않는다. 호출 시점과 표시 형식(순서·색상·포맷) 확정 필요.
- **`HEALTH_MAX`가 실제 엔티티 최대 체력에 반영되지 않는다** — 기본 체력 + HEALTH_MAX에 `HEALTH_MULTIPLY`/`HEALTH_DIVIDE`를 어떻게 적용할지 공식 필요.
- PAPI 수치 포맷(`%uplayer_mana%`가 `12.0`으로 표시), `/ugive` 수량 상한(현재 2304), 장비 GUI가 열린 동안 하단 인벤 더블클릭이 막히는 부작용 수용 여부, 안내 아이템 디자인.
- `item/plugin.yml`의 `test:` 명령은 실행자 없는 죽은 선언 — 삭제 여부 확인 필요.

### ⛔ 추가 설계 결정 대기 (완료 도메인이 제기)
1. **`GearItemMechanic.toGear()`가 `return this`** — Nexo 메커니즘은 아이템당 싱글턴이므로 `Effect` 인스턴스가 전 서버에서 공유된다. `SkillDefinition` 생성자의 `setConversions()`가 그 공유 Effect에 상태를 심으므로 **SkillDefinition을 만들 때마다 전역 상태가 덮어써진다.** 기어별 Effect 인스턴스를 매번 새로 만들 것인지 확정 필요.
2. **`castingTime` 단위** — 쿨타임은 초로 확정됐으나 캐스팅은 미확정(현재 `* 20`으로 초 가정).
3. **스킬 개수 상한 / 이름 길이 상한**(현재 임시 32) / **`/skill delete` 추가 여부**(현재 잘못 만든 스킬을 지울 수 없음).
4. **`/skill`, `/skillcraft`, `/equipment` 권한** — 플레이어 기능이라 개방 상태. `/skill cast`만 op로 제한함.
5. **`/u_enchant`가 op 전용** — 일반 유저 컨텐츠라면 개방 필요.
6. **강화 비용** — 현재 인챈트북 1권만 소모. 경험치/재화 필요 여부.
7. **캐스팅 세부 규칙** — 캐스팅 중 침묵 부여 시 취소? 탈것 탑승 중(현재 이동 차단 우회)? 텔레포트 전면 차단이 맞는가? 노드 체인 중간 사망/퇴장 시 남은 노드 중단?
8. **비용 부분 지불 롤백** — 여러 타입 중 뒤에서 실패하면 앞서 지불한 자원이 환불되지 않음(현재는 스킬 중단).
9. **기어 `power` 음수 허용 여부** (현재 총합만 9 이하로 검사).
10. **GUI 타이틀·안내 문구·아이콘·색상** 은 전부 임의 선택 상태.
11. **`SkillCastListener`가 취소된 `PlayerInteractEvent`를 무시하지 않음** — 보호구역에서도 스킬 발동. 가드 추가 시 어드벤처 모드 정상 발동을 막을 수 있어 확인 필요.

**재개 시 주의**: 미보고 도메인은 감사 반복이 끝나지 않았을 수 있다. 컴파일은 통과하나 각 도메인의 재감사 패스를 다시 돌려야 한다.

### ⛔ 최우선 설계 결정 대기 — BUFF 컨테이너 영속 정책 (치명)
`AttributeBuff`가 `AttributeManager.ContainerType.BUFF`에 값을 더하고 `onRemove`에서 빼는 구조인데,
BUFF 컨테이너는 `entity.getDataMap(plugin)` = **플레이어 PDC에 디스크 영속**되고 **어디서도 초기화되지 않는다**(코드 전체 확인).
→ 서버 크래시·강제종료로 `onRemove`가 돌지 못하면 버프 수치가 **영구 스탯 증가로 잔존**한다. 반복하면 무한 증폭 악용 가능.
버프 도메인 내부의 누락 경로(예외/실패/중복 종료)는 모두 막았으나 크래시는 코드로 막을 수 없다.

선택지:
- (a) 로그인 시 BUFF 컨테이너를 0으로 초기화한 뒤 `buff_suspends` 스냅샷으로 재구성 — 기존 `BuffLifecycleListener.onJoin` 복원 흐름과 정합적
- (b) BUFF 컨테이너를 비영속(메모리) 저장으로 분리 — 3-컨테이너 구조 변경

**구조 결정이므로 사용자 확정 없이는 착수하지 않는다.**

### 확인된 사실 (참고)
- `INVINCIBLE` 버프는 `CastingManager`에서 읽기만 하지만, `BuffEffect`가 `buffType: INVINCIBLE`로 부여 가능하므로 죽은 코드는 아니다.
  단 `InvincibleBuff`는 상태 마커일 뿐이며 **데미지 무효 효과는 없다**(해당 파일 todo에 명시) → 무적이 피해도 막아야 하는지 확정 필요.

---

## 4. 발견 이슈 (작업 중 추가 기록, 임의 수정 금지)

- `GearItemMechanic.setCost()`: YAML 스키마가 `costs.<key>: <타입문자열>`인지 `costs.<타입>: {value: n}`인지에 따라 파싱 로직이 달라짐 — 스키마 확정 필요.
- `AttributeType.EARTH_*` ↔ `ElementalType.LAND` 네이밍 불일치 — 속성 Modifier(T9) 착수 전 확인.
- `PhysicalDefModifier.finalRes`: 저항 100 초과 시 음수 배율 — **사용자 확인: 일부 스탯은 100 초과 불가 설계라 문제없음** (수정 금지).
- 디버그 `sendLog` 다수 — **의도됨, 제거 금지** (사용자가 추후 일괄 제거).
- `randomCriCheck` attacker null 미가드 — **사용자 확인: 굳이 안 해도 됨** (수정 금지).
- `Stat.configSet`의 `AttributeType.valueOf` 즉시 예외 — **사용자 확인: 내부 설정이라 문제없음** (수정 금지).
- **밸런스 todo 상수 목록** (임시값, 사용자 확정 필요):
  - `ElementalPostProcessor`: 감전 지속 100틱 / 화상 지속 100틱 / 침묵 확률 5%(스탯 보정 공식 미정) / 파쇄 지속 100틱·중첩 상한 없음
  - `BurnDebuff`: 도트 주기 20틱
  - `ShatterDefModifier`: 중첩당 +5%
  - `VamfirePostProcessor`: 흡혈 % 공식 전체
  - `ProjectileEffect`: SPREAD 각도 45도 / 발사 높이 1.5
- `EntityDamageListener`가 이미 취소된 이벤트/모든 데미지를 UW 파이프라인으로 넘김 — 낙하/익사 등 환경 데미지도 PHYSICAL로 처리되는 게 의도인지 확인 필요.

---

## 5. 검증 명령

**이 환경에는 mvn 바이너리가 없다** (D:\Maven은 로컬 저장소만 존재, 빌드는 IntelliJ가 수행).
검증 스크립트가 리포지토리에 포함되어 있다 — `tools/compile-check.ps1`:

```bash
powershell -NoProfile -ExecutionPolicy Bypass -File tools/compile-check.ps1 all
```

- 인자: `<target> [tag]` — target은 `core|item|mob|enchant|all`, tag는 병렬 실행 시 출력 격리용(기본 `default`)
- 출력: `target/javac-check/<tag>/` (gitignore 대상)

### 아래는 스크립트 재생성이 필요할 때의 사양 (참고)
javac 직접 컴파일 스크립트로 검증한다 (세션 scratchpad에 없으면 아래 사양으로 재생성):

```bash
powershell -NoProfile -ExecutionPolicy Bypass -File "<scratchpad>/compile.ps1" all
```

스크립트 사양 (재생성 시):
- javac: `D:\jdk\25\bin\javac.exe`, `--release 25 -encoding UTF-8 -nowarn`
- classpath: `D:\Maven\repository` 하위 전체 jar (sources/javadoc 제외), **argfile에 쓸 때 `\` → `/` 변환 필수** (argfile 이스케이프 문제)
- 출력: `D:\Project\UndefinedWorld\target\javac-check\<module>` — **한글 경로 금지** (argfile ASCII 인코딩 문제)
- 순서: core → item(core 출력 cp 추가) → mob(core 출력 cp 추가)
- 인자: `core` / `item` / `mob` / `all`
