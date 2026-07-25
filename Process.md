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

- Maven 멀티모듈: `core`(도메인) / `item`(Nexo 어댑터) / `mob`(MythicMobs 어댑터), Java 25, Paper API 26.1.2
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
