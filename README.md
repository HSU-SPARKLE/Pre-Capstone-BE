# AI 템플릿 메이커
한성대학교 프리캡스톤디자인[8] 4팀 스파클 백엔드 레포지토리

&nbsp;
## 프로젝트 실행 방법
SWPC 2024, 4팀: 스파클 노션에 기록  
https://www.notion.so/swagger-12ecfec7eed1800d9ab4e7b8c577dd94

&nbsp;
## 개요
생성형 AI와 템플릿 기능을 결합하여 소상공인과 소기업(음식점, 카페, 여행사, 소규모 쇼핑몰 등)이 광고 이미지를 손쉽게 제작하고 꾸밀 수 있는 솔루션을 제공합니다.  
사용자는 생성형 AI를 통해 원하는 광고 이미지를 제작한 후 템플릿 기능을 활용해 로고, QR 코드, 텍스트, 추가 이미지를 삽입하여 세련되고 일관성 있는 광고 이미지를 완성할 수 있습니다.  
뿌리오 API와 연동해 생성된 이미지를 원하는 발신자에게 간편하게 문자로 전송할 수 있도록 지원합니다.  
| ![dalle](https://github.com/user-attachments/assets/936bc5b1-60d7-44ab-9087-b539c0b47dc0) | ![로고,QR](https://github.com/user-attachments/assets/01b5f623-bd91-4033-a210-df14e0efabf5) |
|------------------------------------------------------------------------------------------------|------------------------------------------------------------------------------------------------|
| ![텍스트](https://github.com/user-attachments/assets/d717adfd-b375-4c2b-8a6a-c497cca333ae) | ![이미지](https://github.com/user-attachments/assets/030be4b0-5f3e-4849-afbf-fe72f129fd39) |
<table>
  <tr>
    <td><img src="https://github.com/user-attachments/assets/3a21855d-7e8c-478d-b7c2-8712e2749aca" alt="템플릿완성1" width="424" height="600"></td>
    <td><img src="https://github.com/user-attachments/assets/dd71b27d-351e-48f4-9b89-77c4a6d3caaf" alt="템플릿완성2" width="424" height="600"></td>
  </tr>
</table>


&nbsp;
## 주요 기능
### 1. DALL-E 3를 이용한 이미지 생성
- 뿌리오의 요구사항을 반영한 글자와 사람이 포함되지 않은 깔끔하고 미니멀한 디자인의 이미지를 생성합니다.
- 사용자가 광고하고자 하는 의도에 맞게 이미지 및 텍스트를 수월하게 삽입하기 위한 단색 배경 위주의 이미지 생성합니다.
- 스타일 별로 3장의 이미지를 생성하여 사용자가 원하는 스타일의 이미지를 선택할 수 있도록 제공합니다.
  - **애니메이션 스타일**
  - **사실적인 포토 스타일**
  - **일러스트 스타일**
    <img width="544" alt="image" src="https://github.com/user-attachments/assets/4c7b4aef-21ba-4088-a04c-7ce2199d7c4a">


### 2. 템플릿 기능
- **로고 삽입**
  - 기업, 식당, 카페 등의 로고를 로컬 컴퓨터에서 업로드하여 이미지에 삽입해 브랜드 아이덴티티 강화할 수 있습니다.
- **QR 코드 생성 및 삽입**
  - 원하는 텍스트 및 URL을 입력하여 QR 코드를 생성해 광고 이미지에 삽입하여 간편한 사이트 홍보와 수신자는 문자 메시지 없이 이미지를 통해 바로 사이트로 이동 가능합니다.
- **텍스트 삽입**
  - 원하는 폰트, 크기, 색상, 배경 등을 선택해 광고 문구를 스타일에 맞춰 브랜드 이미지에 어울리는 텍스트 삽입 및 편집이 가능합니다.
- **이미지 삽입**
  - 사용자가 입력한 키워드에 맞는 다양한 이미지를 Unsplash API를 통해 제공하며, Remove.bg를 이용해 배경 제거 기능을 제공하여 AI로 생성된 이미지에 자연스럽게 부착이 가능합니다.

### 3. 광고 메시지 생성 및 광고 문자 발송
- OpenAI를 활용하여 사용자가 입력한 발송 목적 및 내용을 분석하고 이를 기반으로 효과적인 광고 문구를 생성합니다.
- 생성된 광고 문자는 엑셀 파일 형태로 업로드된 주소록과 연동되며, 뿌리오 API를 통해 원하는 수신자들에게 손쉽게 발송할 수 있습니다.

&nbsp;
## 백엔드 사용 기술
<p>
<img src="https://img.shields.io/badge/Java-007396?style=flat-square&logo=OpenJDK&logoColor=white"/>  
<img src="https://img.shields.io/badge/SpringBoot-6DB33F?style=flat-square&logo=SpringBoot&logoColor=white" />  
<img src="https://img.shields.io/badge/MySQL-4479A1?style=flat-square&logo=mysql&logoColor=white" />  
</p>

### Azure OpenAI DALL-E 3
- 사용자가 입력한 발송 목적 및 내용을 바탕으로 프롬프트를 가공하여 Azure OpenAI DALL-E 3를 통해 이미지를 생성.
- 일러스트 스타일, 애니메이션 스타일, 사실적인 포토 스타일 총 3개의 스타일 별로 이미지 생성을 요청하여 사용자는 원하는 스타일의 이미지를 선택 가능.
- Azure OpenAI Service를 이용하여 DALLE-3 모델을 사용할 경우 Azure Blob Storage, Azure Text Analytics 등 Azure의 다양한 서비스와 쉽게 연동하여 이미지 저장, 추가 데이터 분석 등을 원활하게 처리할 수 있다는 장점.

### OpenAI
- 사용자가 입력한 발송 목적 및 내용을 OpenAI API를 통해 영어로 번역하여, DALL-E에 전달할 프롬프트에 활용.
- 사용자가 입력한 발송 목적 및 내용을 기반으로 적합한 광고 문자 메시지 생성.

### Azure Text Analytics
- 사용자가 입력한 이미지 생성 목적에서 키워드를 추출하여 해당 키워드들이 이미지 생성에 잘 반영되도록 미리 정해둔 프롬프트 형식에 포함하여 사용.

### Azure Blob Storage
- 템플릿 기능을 통해 완성된 이미지를 Azure Blob Storage에 저장하여 클라우드에서 안전하게 관리.

&nbsp;
## 이미지 생성 prompt
애니메이션, 일러스트, 사실적인 포토 스타일의 각기 다른 이미지 3장을 생성합니다.  
이미지 생성에 사용될 주요 키워드는 사용자가 입력한 발송 목적과 내용을 기반으로 Azure Text Analytics를 활용하여 추출합니다.  
생성된 이미지에는 텍스트와 사람이 포함되지 않으며, 배경은 단순한 단색 디자인으로 구성되어 복잡하거나 산만한 요소가 들어가지 않도록 합니다.  
또한, 사용자가 드롭다운에서 선택한 이미지 분위기(차분한 분위기, 활기찬 분위기, 따뜻한 느낌)와 계절감을 반영하여 보다 목적에 부합하는 이미지를 제공합니다.  
영어로 작성된 프롬프트를 활용하여 모델의 성능을 최적화하고, 일관성과 품질이 뛰어난 이미지를 생성할 수 있도록 지원합니다.

&nbsp;
## 시스템 구조
![프캡 구조도](https://github.com/user-attachments/assets/922c329b-1fe0-4a90-93ea-3b1729c901b4)

&nbsp;
## 백엔드 구성 및 역할 분담

| 이름       | 역할                                                                 |
|------------|----------------------------------------------------------------------|
| 박미정     | 이미지 생성 로직 & 광고 문자 생성 로직 구현, AI 프롬프트 최적화          |
| 진현성     | DB 및 Azure Blob Storage 연동, 백엔드-프론트엔드 연결                 |
| 홍승기     | 뿌리오 API 연결, 문자 발송 기능 구현                                  |

