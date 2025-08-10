from anyio.to_process import WORKER_MAX_IDLE_TIME
from torch.utils.data.datapipes.map.utils import SequenceWrapperMapDataPipe

from integrations.lse.fetch_instrument import (
    extract_lse_data_with_browser,
    create_instrument,
)

if __name__ == "__main__":
    xml_file = (
        "C:/Users/steph/workspaces/luk/data/portfolio/investments-with-id-updated.xml"
    )
    output_file = "C:/Users/steph/workspaces/luk/data/portfolio/investments-with-id.xml"
    xml_file = output_file

    url = "https://www.londonstockexchange.com/stock/VMID/vanguard/company-page"

    create_instrument(url=url, xml_file=xml_file, output_file=output_file)
